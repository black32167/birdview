package org.birdview.web

import org.birdview.config.BVOAuthSourceConfig
import org.birdview.config.BVRuntimeConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.source.gdrive.GAccessTokenResponse
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form

@RestController()
@RequestMapping("/oauth")
class BVOAuthController(
        val bvRuntimeConfig: BVRuntimeConfig,
        val sourceConfigProvider: BVSourcesConfigProvider
) {
    interface CodeCallback {
        fun onCode(code: String)
    }
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
            code != null -> getAndSaveRefreshToken(source = source, authCode = code)
            else -> log.error("OAuth authentication error for source ${source}:no code provided!")
        }
        return ModelAndView("redirect:/settings")
    }

    fun getToken(config: BVOAuthSourceConfig): String? =
            loadLocalRefreshToken(config.sourceName)?.let { refreshToken-> getRemoteAccessToken(refreshToken, config) }

    fun hasToken(config: BVOAuthSourceConfig): Boolean =
            loadLocalRefreshToken(config.sourceName) != null;

    private fun getRemoteAccessToken(refreshToken: String, config: BVOAuthSourceConfig): String =
            WebTargetFactory(config.tokenExchangeUrl)
                    .getTarget("")
                    .request()
                    .post(getTokenRefreshFormEntity(refreshToken, config))
                    .also { response ->
                        if(response.status != 200) {
                            throw RuntimeException("Error reading Google access token: ${response.readEntity(String::class.java)}")
                        }
                    }
                    .readEntity(GAccessTokenResponse::class.java)
                    .access_token

    private fun getTokenExchangeFormEntity(authCode:String, config: BVOAuthSourceConfig) =
            Entity.form(Form()
                    .param("client_id", config.clientId)
                    .param("client_secret", config.clientSecret)
                    .param("access_type", "offline")
                    .param("code", authCode)
                    .param("grant_type", "authorization_code")
                    .param("redirect_uri", getRedirectUrl(config.sourceName)))

    private fun getTokenRefreshFormEntity(refreshToken:String, config: BVOAuthSourceConfig) =
            Entity.form(Form()
                    .param("client_id", config.clientId)
                    .param("client_secret", config.clientSecret)
                    .param("grant_type", "refresh_token")
                    .param("refresh_token", refreshToken))

    private fun loadLocalRefreshToken(source: String):String? =
        getRefreshTokenFileName(source)
                .takeIf { refreshTokenFile -> Files.exists(refreshTokenFile) }
                ?.let { refreshTokenFile -> Files.readAllLines(refreshTokenFile).firstOrNull() }

    fun getRedirectUrl(source: String) =
        "${getBaseUrl()}/oauth/code?source=${source}"

    fun getAuthTokenUrl(oAuthConfig: BVOAuthSourceConfig): String {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=${oAuthConfig.clientId}" +
                "&response_type=code" +
                "&redirect_uri=${getRedirectUrl(oAuthConfig.sourceName)}" +
                "&scope=${oAuthConfig.scope}"
    }

    private fun getAndSaveRefreshToken(source: String, authCode:String) {
        val config = sourceConfigProvider.getConfigByName(source) as BVOAuthSourceConfig
        getTokensResponse(authCode, config) ?.also { tokenResponse ->

            val refreshTokenFile = getRefreshTokenFileName(source)
            Files.createDirectories(refreshTokenFile.parent)
            Files.write(refreshTokenFile, listOf(tokenResponse.refresh_token),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }

    private fun getTokensResponse(authCode: String, config: BVOAuthSourceConfig): GAccessTokenResponse? =
            WebTargetFactory(config.tokenExchangeUrl)
                    .getTarget("")
                    .request()
                    .post(getTokenExchangeFormEntity(authCode, config))
                    .also { response ->
                        if(response.status != 200) {
                            throw RuntimeException("Error reading Google access token: ${response.readEntity(String::class.java)}")
                        }
                    }
                    .readEntity(GAccessTokenResponse::class.java)

    private fun getRefreshTokenFileName(source: String) =
            bvRuntimeConfig.oauthTokenDir.resolve("${source}.token")

    private fun getBaseUrl() =
            ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
}