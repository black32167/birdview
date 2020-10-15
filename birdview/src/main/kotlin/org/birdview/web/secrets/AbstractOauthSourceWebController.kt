package org.birdview.web.secrets

import org.birdview.storage.BVOAuthSourceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.servlet.view.RedirectView
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form
import javax.ws.rs.core.Response

abstract class AbstractOauthSourceWebController<T : BVOAuthSourceConfig, F>(
        sourceSecretsStorage: BVSourceSecretsStorage
): AbstractSourceWebController<T, F>(sourceSecretsStorage) {
    companion object {
        const val CODE_ENDPOINT_PATH = "/code"
    }
    private val log = LoggerFactory.getLogger(AbstractOauthSourceWebController::class.java)

    protected abstract fun consumeAuthCodeExchangeResponse(sourceName: String, rawResponse: Response)

    protected abstract fun getControllerPath():String

    @GetMapping(CODE_ENDPOINT_PATH)
    fun onAuthorizationCode(
            @RequestParam(value = "source", required = true) source: String,
            @RequestParam(value = "code", required = false) code: String?,
            @RequestParam(value = "error", required = false) maybeError: String?,
            @RequestParam(value = "scope", required = false) scope: String?
    ): ModelAndView {
        when {
            maybeError != null -> log.error("OAuth authentication error for source ${source}:${maybeError}")
            code != null -> exchangeAuthorizationCode(sourceName = source, authCode = code)
            else -> log.error("OAuth authentication error for source ${source}:no code provided!")
        }
        return ModelAndView("redirect:/secrets")
    }

    override fun getRedirectAfterSaveView(config: T): Any =
            RedirectView(getProviderAuthCodeUrl(config))

    private fun getProviderAuthCodeUrl(oAuthConfig: BVOAuthSourceConfig): String {
        return oAuthConfig.authCodeUrl +
                "client_id=${oAuthConfig.clientId}" +
                "&response_type=code" +
                "&redirect_uri=${getRedirectCodeUrl(oAuthConfig.sourceName)}" +
                "&scope=${oAuthConfig.scope}"
    }

    private fun exchangeAuthorizationCode(sourceName: String, authCode:String) {
        val config = sourceSecretsStorage.getConfigByName(sourceName) as BVOAuthSourceConfig
        val formEntity = Entity.form(Form()
                .param("client_id", config.clientId)
                .param("client_secret", config.clientSecret)
                .param("access_type", "offline")
                .param("code", authCode)
                .param("grant_type", "authorization_code")
                .param("redirect_uri", getRedirectCodeUrl(config.sourceName)))
        val authCodeExchangeResponse = WebTargetFactory(config.tokenExchangeUrl)
                .getTarget("")
                .request()
                .post(formEntity)
                .also { response ->
                    if (response.status != 200) {
                        throw RuntimeException("Error reading access token: ${response.readEntity(String::class.java)}")
                    }
                }
        consumeAuthCodeExchangeResponse(sourceName, authCodeExchangeResponse)
    }

    protected fun getRedirectCodeUrl(source: String) =
            "${ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()}/${getControllerPath()}/${CODE_ENDPOINT_PATH}?source=${source}"
}