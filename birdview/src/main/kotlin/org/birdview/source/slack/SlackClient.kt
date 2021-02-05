package org.birdview.source.slack

import org.birdview.analysis.BVDocument
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.source.oauth.AbstractOAuthClient
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.source.slack.model.SlackMessage
import org.birdview.storage.BVOAuthSourceConfig
import org.birdview.storage.BVSlackConfig
import javax.inject.Named

@Named
class SlackClient  (
    private val httpClientFactory: BVHttpClientFactory,
    tokenStorage: OAuthRefreshTokenStorage
): AbstractOAuthClient<SlackTokenResponse>(tokenStorage, httpClientFactory) {
//    private val targetFactory =
//            WebTargetFactory("https://slack.com/api") {
//                getToken(config)
//                        ?.let(::BearerAuth)
//                        ?: throw RuntimeException("Failed retrieving Slack API access token")
//            }

    fun findMessages(sourceConfig: BVSlackConfig, chunkConsumer: (List<BVDocument>) -> Unit) {
//        var r = targetFactory.getTarget("search.messages")
//                .queryParam("query", "from:@...")
//                .request()
//                .get()
//                .readEntity(String::class.java)
//        var r1 = targetFactory.getTarget("conversations.history")
//                .queryParam("channel", "C01B2T1CZBM")
//                .request()
//                .get()
//                .readEntity(String::class.java)
//            val resp = targetFactory.getTarget("conversations.history")
//                .queryParam("channel", "???")
//                .request()
//                .get()
//        val response = resp.readEntity(SlackHistoryResponse::class.java)
//        response.messages
//                ?.mapNotNull { toBVDocument(it) }
//                ?.also { docs->
//                    chunkConsumer.invoke(docs)
//                }
    }

    private fun toBVDocument(slackMessage: SlackMessage): BVDocument? {
        return null
    }

    override fun getToken(config: BVOAuthSourceConfig): String? =
            tokenStorage.getAccessToken(config)

    override fun getTokenRefreshFormContent(refreshToken:String, config: BVOAuthSourceConfig): Map<String, String> =
            mapOf(
                    "client_id" to config.clientId,
                    "client_secret" to config.clientSecret,
                 //   "grant_type" to "refresh_token",
                    "code" to refreshToken)

    override fun readAccessTokenResponse(response: SlackTokenResponse): String = response.access_token!!
    override fun getAccessTokenResponseClass(): Class<SlackTokenResponse> = SlackTokenResponse::class.java
}