package org.birdview.source.slack

import org.birdview.analysis.BVDocument
import org.birdview.config.sources.BVOAuthSourceConfig
import org.birdview.config.sources.BVSlackConfig
import org.birdview.source.oauth.AbstractOAuthClient
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.source.slack.model.SlackMessage
import org.birdview.utils.remote.BearerAuth
import org.birdview.utils.remote.WebTargetFactory
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form
import javax.ws.rs.core.Response

class SlackClient  (
        private val config: BVSlackConfig,
        tokenStorage: OAuthRefreshTokenStorage
): AbstractOAuthClient(tokenStorage) {
    private val targetFactory =
            WebTargetFactory("https://slack.com/api") {
                getToken(config)
                        ?.let(::BearerAuth)
                        ?: throw RuntimeException("Failed retrieving Slack API access token")
            }

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

    override fun getTokenRefreshFormEntity(refreshToken:String, config: BVOAuthSourceConfig): Entity<Form> =
            Entity.form(Form()
                    .param("client_id", config.clientId)
                    .param("client_secret", config.clientSecret)
                 //   .param("grant_type", "refresh_token")
                    .param("code", refreshToken))

    override fun readAccessTokenResponse(response: Response): String = response
            .readEntity(SlackTokenResponse::class.java)
            .also {
                print(it)
            }
            .access_token!!
}