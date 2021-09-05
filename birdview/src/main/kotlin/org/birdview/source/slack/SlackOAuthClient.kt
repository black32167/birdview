package org.birdview.source.slack

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.source.oauth.AbstractOAuthClient
import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.model.BVOAuthTokens
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import javax.inject.Named

@Named
class SlackOAuthClient (
    httpClientFactory: BVHttpClientFactory,
    tokenStorage: OAuthTokenStorage
) : AbstractOAuthClient<SlackTokenResponse>(tokenStorage, httpClientFactory) {
    override fun getTokenRefreshFormContent(refreshToken:String, config: BVOAuthSourceSecret): Map<String, String> =
        mapOf(
            "client_id" to config.clientId,
            "client_secret" to config.clientSecret,
            //   "grant_type" to "refresh_token",
            "code" to refreshToken)

    override fun extractTokensData(response: SlackTokenResponse): BVOAuthTokens =
        BVOAuthTokens(
            accessToken = response.access_token!!,
            refreshToken = null,
            expiresTimestamp = null
        )

    override fun getAccessTokenResponseClass(): Class<SlackTokenResponse> = SlackTokenResponse::class.java

    override fun saveOAuthTokens(bvUser: String, sourceName: String, rawResponse: SlackTokenResponse) {
        val tokensData = extractTokensData(rawResponse)
        defaultTokenStorage.saveOAuthTokens(bvUser = bvUser, sourceName = sourceName, tokensData)
    }
}