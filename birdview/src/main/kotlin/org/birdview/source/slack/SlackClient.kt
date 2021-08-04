package org.birdview.source.slack

import org.birdview.analysis.BVDocument
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.storage.model.secrets.BVSlackConfig
import javax.inject.Named

@Named
class SlackClient  (
    private val httpClientFactory: BVHttpClientFactory,
    private val oauthClient: SlackOAuthClient
) {
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

}