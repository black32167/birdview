package org.birdview.web.secrets

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.storage.BVOAuthSourceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.web.BVWebPaths
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.servlet.view.RedirectView

abstract class AbstractOauthSourceWebController<AuthCodeResponse, T : BVOAuthSourceConfig, F>(
    private val httpClientFactory: BVHttpClientFactory,
    sourceSecretsStorage: BVSourceSecretsStorage,
): AbstractSourceWebController<T, F>(sourceSecretsStorage) {
    companion object {
        const val CODE_ENDPOINT_PATH = "code"
    }
    private val log = LoggerFactory.getLogger(AbstractOauthSourceWebController::class.java)

    protected abstract fun consumeAuthCodeExchangeResponse(sourceName: String, rawResponse: AuthCodeResponse)
    protected abstract fun getAuthCodeResponseClass(): Class<AuthCodeResponse>

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
        return ModelAndView("redirect:${BVWebPaths.ADMIN_ROOT}")
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
        val authCodeExchangeResponse =
            httpClientFactory.getHttpClient(config.tokenExchangeUrl).postForm(
                resultClass = getAuthCodeResponseClass(),
                formFields = mapOf(
                    "client_id" to config.clientId,
                    "client_secret" to config.clientSecret,
                    "access_type" to "offline",
                    "code" to authCode,
                    "grant_type" to "authorization_code",
                    "redirect_uri" to getRedirectCodeUrl(config.sourceName))
                )
        consumeAuthCodeExchangeResponse(sourceName, authCodeExchangeResponse)
    }

    protected fun getRedirectCodeUrl(source: String) =
            "${ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()}${getControllerPath()}/${CODE_ENDPOINT_PATH}?source=${source}"
}