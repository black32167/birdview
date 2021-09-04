package org.birdview.source.oauth

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.model.BVOAuthTokens
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.slf4j.LoggerFactory

abstract class AbstractOAuthClient<RT: OAuthTokenResponse>(
    protected val defaultTokenStorage: OAuthTokenStorage,
    private val httpClientFactory: BVHttpClientFactory
) {
    private val log = LoggerFactory.getLogger(AbstractOAuthClient::class.java)

    open fun getToken(config: BVOAuthSourceSecret): String? =
        defaultTokenStorage.loadOAuthTokens(config.sourceName)
            ?.let { tokens ->
                val validAccessToken: String? = tokens.accessToken
                    .takeUnless { tokens.isExpired() }
                    ?: tokens.refreshToken?.let { refreshToken ->
                        val renewedTokens = getRemoteAccessToken(config, refreshToken)
                        defaultTokenStorage.saveOAuthTokens(config.sourceName, renewedTokens)
                        renewedTokens.accessToken
                    }
                validAccessToken
            }

    private fun getRemoteAccessToken(config: BVOAuthSourceSecret, refreshToken: String): BVOAuthTokens {
        return httpClientFactory.getHttpClientUnauthenticated(config.tokenExchangeUrl)
            .postForm(
                resultClass = getAccessTokenResponseClass(),
                formFields = getTokenRefreshFormContent(refreshToken, config)
            )
            .let { extractTokensData(it) } //todo: save (same as in web controller)
    }

    protected abstract fun getTokenRefreshFormContent(
        refreshToken: String,
        config: BVOAuthSourceSecret
    ): Map<String, String>

    protected abstract fun extractTokensData(response: RT): BVOAuthTokens
    abstract fun getAccessTokenResponseClass(): Class<RT>

    protected abstract fun saveOAuthTokens(sourceName: String, rawResponse: RT)

    fun updateAccessToken(sourceName: String,
                          authCode: String,
                          redirectUrl: String,
                          oauthSecret: BVOAuthSourceSecret) {
        val fields = mapOf(
            "client_id" to oauthSecret.clientId,
            "client_secret" to oauthSecret.clientSecret,
            "access_type" to "offline",
            "code" to authCode,
            "grant_type" to "authorization_code",
            "redirect_uri" to redirectUrl
        )
        val accessTokenExchangeResponse =
            httpClientFactory.getHttpClientUnauthenticated(oauthSecret.tokenExchangeUrl).postForm(
                resultClass = getAccessTokenResponseClass(),
                formFields = fields)
        saveOAuthTokens(sourceName, accessTokenExchangeResponse);
    }
}
