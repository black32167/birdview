package org.birdview.source.confluence

import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.confluence.model.ConfluenceSearchItem
import org.birdview.source.confluence.model.ConfluenceSearchItemContent
import org.birdview.source.confluence.model.ConfluenceSearchPageResponseSearchResult
import org.birdview.source.confluence.model.ContentArray
import org.birdview.source.http.BVHttpSourceClientFactory
import org.birdview.utils.BVTimeUtil
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class ConfluenceClient(
    private val httpClientFactory: BVHttpSourceClientFactory
) {
    private val log = LoggerFactory.getLogger(ConfluenceClient::class.java)
    private val documentsPerPage = 50
    private val pageExpands = listOf(
        "history.contributors.publishers.users",
        "version"
    )

    fun findDocuments(
        config: BVSourceConfigProvider.SyntheticSourceConfig,
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
                val response = BVTimeUtil.logTimeAndReturn("confluence-findDocs-page") {
                    getHttpClient(config).get(
                        resultClass = ConfluenceSearchPageResponseSearchResult::class.java,
                        subPath = "search",
                        parameters = mapOf<String, Any>(
                            "start" to startAt,
                            "limit" to documentsPerPage,
                            "cql" to cql,
                            "expand" to pageExpands.map { "content.${it}" }.joinToString (",")
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

    fun loadPage(config: BVSourceConfigProvider.SyntheticSourceConfig, pageUrl: String): ConfluenceSearchItemContent =
        getHttpClient(config, pageUrl).get(
            resultClass = ConfluenceSearchItemContent::class.java,
            parameters = mapOf("expand" to pageExpands.joinToString(","))
        )

    fun loadComments(config: BVSourceConfigProvider.SyntheticSourceConfig, pageId: String): List<ConfluenceSearchItemContent> =
        getHttpClient(config).get(
            resultClass = ContentArray::class.java,
            subPath = "content/${pageId}/child/comment",
            parameters = mapOf(
                "limit" to 100,
                "expand" to pageExpands.joinToString(","))
        ).results

    private fun getHttpClient(config: BVSourceConfigProvider.SyntheticSourceConfig) =
        getHttpClient(config, "${config.baseUrl}/rest/api")

    private fun getHttpClient(config: BVSourceConfigProvider.SyntheticSourceConfig, url: String) =
        httpClientFactory.createClient(config)

}
