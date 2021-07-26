package org.birdview.source.oauth

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.storage.BVOAuthSourceConfig
import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.model.BVOAuthTokens

abstract class AbstractOAuthClient<RT>(
    protected val tokenStorage: OAuthTokenStorage,
    private val httpClientFactory: BVHttpClientFactory
) {
    open fun getToken(config: BVOAuthSourceConfig): String? =
        tokenStorage.loadOAuthTokens(config.sourceName)
            ?.let { tokens->
                val expired = tokens.expiresTimestamp
                    ?.let { it < System.currentTimeMillis() }
                    ?: false
                val validAccessToken:String? = tokens.accessToken
                    .takeUnless { expired }
                    ?: tokens.refreshToken ?.let { refreshToken->
                        val renewedTokens = getRemoteAccessToken(config, refreshToken)
                        tokenStorage.saveOAuthTokens(config.sourceName, renewedTokens)
                        renewedTokens.accessToken
                    }
                validAccessToken
            }

    private fun getRemoteAccessToken(config: BVOAuthSourceConfig, refreshToken: String): BVOAuthTokens {
        return httpClientFactory.getHttpClient(config.tokenExchangeUrl)
            .postForm(
                resultClass = getAccessTokenResponseClass(),
                formFields = getTokenRefreshFormContent(refreshToken, config))
            .let { extractTokensData(it) } //todo: save (same as in web controller)
    }

    protected abstract fun getTokenRefreshFormContent(refreshToken:String, config: BVOAuthSourceConfig): Map<String, String>
    protected abstract fun extractTokensData(response: RT): BVOAuthTokens
    protected abstract fun getAccessTokenResponseClass(): Class<RT>

    fun isAuthenticated(sourceName:String) = tokenStorage.loadOAuthTokens(sourceName) != null
}
