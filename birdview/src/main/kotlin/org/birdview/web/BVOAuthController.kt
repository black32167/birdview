package org.birdview.web

import org.birdview.config.BVOAuthSourceConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.source.gdrive.GAccessTokenResponse
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form

@RestController()
@RequestMapping("/oauth")
class BVOAuthController(
        val tokenStorage: OAuthRefreshTokenStorage,
        val sourceConfigProvider: BVSourcesConfigProvider
) {
    private val log = LoggerFactory.getLogger(BVOAuthController::class.java)

    @GetMapping("/code")
    fun setCode(
            @RequestParam(value = "source", required = true) source: String,
            @RequestParam(value = "code", required = false) code: String?,
            @RequestParam(value = "error", required = false) maybeError: String?,
            @RequestParam(value = "scope", required = false) scope: String?
    ): ModelAndView {
        when {
            maybeError != null -> log.error("OAuth authentication error for source ${source}:${maybeError}")
            code != null -> getAndSaveRefreshToken(sourceName = source, authCode = code)
            else -> log.error("OAuth authentication error for source ${source}:no code provided!")
        }
        return ModelAndView("redirect:/settings")
    }

    private fun getTokenExchangeFormEntity(authCode:String, config: BVOAuthSourceConfig) =
            Entity.form(Form()
                    .param("client_id", config.clientId)
                    .param("client_secret", config.clientSecret)
                    .param("access_type", "offline")
                    .param("code", authCode)
                    .param("grant_type", "authorization_code")
                    .param("redirect_uri", getRedirectUrl(config.sourceName)))


    fun getRedirectUrl(source: String) =
        "${getBaseUrl()}/oauth/code?source=${source}"

    fun getAuthTokenUrl(oAuthConfig: BVOAuthSourceConfig): String {
        return oAuthConfig.authCodeUrl +
                "client_id=${oAuthConfig.clientId}" +
                "&response_type=code" +
                "&redirect_uri=${getRedirectUrl(oAuthConfig.sourceName)}" +
                "&scope=${oAuthConfig.scope}"
    }

    private fun getAndSaveRefreshToken(sourceName: String, authCode:String) {
        val config = sourceConfigProvider.getConfigByName(sourceName) as BVOAuthSourceConfig
        getTokensResponse(authCode, config) ?.also { tokenResponse ->
            if(tokenResponse.refresh_token != null) {
                tokenStorage.saveRefreshToken(sourceName, tokenResponse.refresh_token)
            } else {
                tokenStorage.saveAccessToken(sourceName, tokenResponse.access_token)
            }
        }
    }

    private fun getTokensResponse(authCode: String, config: BVOAuthSourceConfig): GAccessTokenResponse? =
            WebTargetFactory(config.tokenExchangeUrl)
                    .getTarget("")
                    .request()
                    .post(getTokenExchangeFormEntity(authCode, config))
                    .also { response ->
                        if(response.status != 200) {
                            throw RuntimeException("Error reading access token: ${response.readEntity(String::class.java)}")
                        }
                    }
                    .readEntity(GAccessTokenResponse::class.java)


    private fun getBaseUrl() =
            ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
}