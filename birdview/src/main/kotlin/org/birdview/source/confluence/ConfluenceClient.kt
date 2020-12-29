package org.birdview.source.confluence

import org.birdview.source.confluence.model.ConfluenceSearchItem
import org.birdview.source.confluence.model.ConfluenceSearchPageResponseSearchResult
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.storage.BVConfluenceConfig
import org.birdview.utils.BVTimeUtil
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import java.lang.Integer.min
import javax.inject.Named
import javax.ws.rs.client.WebTarget

@Named
class ConfluenceClient(
    private val httpClientFactory: BVHttpClientFactory
) {
    private val log = LoggerFactory.getLogger(ConfluenceClient::class.java)
    private val documentsPerPage = 50

    fun findDocuments(
        config: BVConfluenceConfig,
        cql: String?,
        chunkConsumer: (List<ConfluenceSearchItem>) -> Unit
    ) {
        if (cql == null) {
            return
        }
        log.info("Running cql '{}'", cql)

        var startAt: Int = 0
        try {
            do {
                val response = BVTimeUtil.logTime("confluence-findDocs-page") {
                    getHttpClient(config).get(
                        resultClass = ConfluenceSearchPageResponseSearchResult::class.java,
                        subPath = "search",
                        parameters = mapOf<String, Any>(
                            "start" to startAt,
                            "limit" to documentsPerPage,
                            "cql" to cql,
                            "expand" to "content.history" +
                                    ",content.history.contributors.publishers" +
                                    ",content.history.contributors.publishers.users" +
                                    ",content.metadata.comments" //TODO: << how to extract comments?
                        )
                    ).also {
                        log.info("Loaded {} confluence pages", it.results.size)
                        chunkConsumer(it.results)
                    }
                }
                startAt = response.start + response.size
            } while (startAt < response.totalSize)
        } catch (e: Exception) {
            log.error("", e)
        }
    }

    private fun getHttpClient(config: BVConfluenceConfig) =
        httpClientFactory.getHttpClient("${config.baseUrl}/rest/api") {
            BasicAuth(config.user, config.token)
        }
}
