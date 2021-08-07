package org.birdview.source.oauth

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.model.BVOAuthTokens
import org.birdview.storage.model.secrets.BVOAuthSourceSecret
import org.slf4j.LoggerFactory

abstract class AbstractOAuthClient<RT>(
    protected val defaultTokenStorage: OAuthTokenStorage,
    private val httpClientFactory: BVHttpClientFactory
) {
    private val log = LoggerFactory.getLogger(AbstractOAuthClient::class.java)

    open fun getToken(config: BVOAuthSourceSecret): String? =
        defaultTokenStorage.loadOAuthTokens(config.sourceName)
            ?.let { tokens->
                val validAccessToken:String? = tokens.accessToken
                    .takeUnless { tokens.isExpired() }
                    ?: tokens.refreshToken ?.let { refreshToken->
                        val renewedTokens = getRemoteAccessToken(config, refreshToken)
                        defaultTokenStorage.saveOAuthTokens(config.sourceName, renewedTokens)
                        renewedTokens.accessToken
                    }
                validAccessToken
            }

    private fun getRemoteAccessToken(config: BVOAuthSourceSecret, refreshToken: String): BVOAuthTokens {
        return httpClientFactory.getHttpClient(config.tokenExchangeUrl)
            .postForm(
                resultClass = getAccessTokenResponseClass(),
                formFields = getTokenRefreshFormContent(refreshToken, config))
            .let { extractTokensData(it) } //todo: save (same as in web controller)
    }

    protected abstract fun getTokenRefreshFormContent(refreshToken:String, config: BVOAuthSourceSecret): Map<String, String>
    protected abstract fun extractTokensData(response: RT): BVOAuthTokens
    protected abstract fun getAccessTokenResponseClass(): Class<RT>
}
