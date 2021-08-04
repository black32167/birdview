package org.birdview.source.gdrive

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.source.oauth.AbstractOAuthClient

import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.model.BVOAuthTokens
import org.birdview.storage.model.secrets.BVOAuthSourceConfig
import javax.inject.Named

@Named
class GDriveOAuthClient(
    httpClientFactory: BVHttpClientFactory,
    tokenStorage: OAuthTokenStorage
) : AbstractOAuthClient<GAccessTokenResponse>(tokenStorage, httpClientFactory) {

    override fun getTokenRefreshFormContent(refreshToken:String, config: BVOAuthSourceConfig): Map<String, String> =
        mapOf(
            "client_id" to config.clientId,
            "client_secret" to config.clientSecret,
            "grant_type" to "refresh_token",
            "refresh_token" to refreshToken)

    override fun extractTokensData(response: GAccessTokenResponse): BVOAuthTokens =
        BVOAuthTokens(
            accessToken = response.access_token,
            refreshToken = response.refresh_token,
            expiresTimestamp = response.expires_in ?.let { System.currentTimeMillis() + it.toLong()*1000 }
        )

    override fun getAccessTokenResponseClass(): Class<GAccessTokenResponse> = GAccessTokenResponse::class.java

    fun saveOAuthTokens(sourceName: String, rawResponse: GAccessTokenResponse) {
        val tokensData = extractTokensData(rawResponse)
        defaultTokenStorage.saveOAuthTokens(sourceName, tokensData)
    }
}