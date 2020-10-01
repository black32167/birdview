package org.birdview.source.oauth

import org.birdview.config.sources.BVOAuthSourceConfig
import org.birdview.utils.remote.WebTargetFactory
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form
import javax.ws.rs.core.Response

abstract class AbstractOAuthClient(
        protected val tokenStorage: OAuthRefreshTokenStorage
) {
    protected open fun getToken(config: BVOAuthSourceConfig): String? =
            tokenStorage.loadLocalRefreshToken(config.sourceName)?.let { refreshToken-> getRemoteAccessToken(refreshToken, config) }

    private fun getRemoteAccessToken(refreshToken: String, config: BVOAuthSourceConfig): String {
        val accessToken = tokenStorage.getAccessToken(config)
        if (accessToken != null) {
            return accessToken
        }
        return WebTargetFactory(config.tokenExchangeUrl)
                .getTarget("")
                .request()
                .post(getTokenRefreshFormEntity(refreshToken, config))
                .also { response ->
                    if (response.status != 200) {
                        throw RuntimeException("Error reading access token: ${response.readEntity(String::class.java)}")
                    }
                }
                .let { readAccessTokenResponse(it) }
    }

    protected abstract fun getTokenRefreshFormEntity(refreshToken:String, config: BVOAuthSourceConfig): Entity<Form>
    protected abstract fun readAccessTokenResponse(response: Response): String
}