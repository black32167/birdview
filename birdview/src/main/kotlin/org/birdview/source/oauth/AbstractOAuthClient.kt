package org.birdview.source.oauth

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.storage.BVOAuthSourceConfig
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form

abstract class AbstractOAuthClient<RT>(
        protected val tokenStorage: OAuthRefreshTokenStorage,
        private val httpClientFactory: BVHttpClientFactory
) {
    protected open fun getToken(config: BVOAuthSourceConfig): String? =
            tokenStorage.loadLocalRefreshToken(config.sourceName)?.let { refreshToken-> getRemoteAccessToken(refreshToken, config) }

    private fun getRemoteAccessToken(refreshToken: String, config: BVOAuthSourceConfig): String {
        val accessToken = tokenStorage.getAccessToken(config)
        if (accessToken != null) {
            return accessToken
        }
        return httpClientFactory.getHttpClient(config.tokenExchangeUrl)
            .postForm(
                resultClass = getAccessTokenResponseClass(),
                formFields = getTokenRefreshFormContent(refreshToken, config))
            .let { readAccessTokenResponse(it) }
    }

    protected abstract fun getTokenRefreshFormContent(refreshToken:String, config: BVOAuthSourceConfig): Map<String, String>
    protected abstract fun readAccessTokenResponse(response: RT): String
    protected abstract fun getAccessTokenResponseClass(): Class<RT>
}
