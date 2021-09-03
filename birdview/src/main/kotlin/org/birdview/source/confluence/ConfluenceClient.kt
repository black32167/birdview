package org.birdview.source.confluence

import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.confluence.model.ConfluenceSearchItem
import org.birdview.source.confluence.model.ConfluenceSearchItemContent
import org.birdview.source.confluence.model.ConfluenceSearchPageResponseSearchResult
import org.birdview.source.confluence.model.ContentArray
import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.BVHttpSourceClientFactory
import org.birdview.utils.BVTimeUtil
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class ConfluenceClient(
    private val httpClientFactory: BVHttpSourceClientFactory,
    private val sourceConfigProvider: BVSourceConfigProvider,
) {
    companion object {
        private const val API_SUFFIX = "/rest/api"
    }
    private val log = LoggerFactory.getLogger(ConfluenceClient::class.java)
    private val documentsPerPage = 50
    private val pageExpands = listOf(
        "history.contributors.publishers.users",
        "version"
    )

    fun findDocuments(
        bvUser: String,
        sourceName:String,
        cql: String?,
        chunkConsumer: (List<ConfluenceSearchItem>) -> Unit
    ) {
        if (cql == null) {
            return
        }
        log.info("Running cql '{}'", cql)

        val sourceConfig = sourceConfigProvider.getSourceConfig(bvUser = bvUser, sourceName = sourceName)
        var startAt: Int = 0
        try {
            do {
                val response = BVTimeUtil.logTimeAndReturn("confluence-findDocs-page") {
                    getHttpClient(bvUser, sourceName, getApiUrl(sourceConfig.baseUrl)).get(
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

    fun loadPage(bvUser: String, sourceName: String, pageUrl: String): ConfluenceSearchItemContent =
        getHttpClient(bvUser = bvUser, sourceName = sourceName, url = pageUrl).get(
            resultClass = ConfluenceSearchItemContent::class.java,
            parameters = mapOf("expand" to pageExpands.joinToString(","))
        )

    fun loadComments(bvUser: String, sourceName: String, pageId: String): List<ConfluenceSearchItemContent> {
        val sourceConfig = sourceConfigProvider.getSourceConfig(sourceName = sourceName, bvUser = bvUser)
        return getHttpClient(bvUser = bvUser, sourceName = sourceName, url = getApiUrl(sourceConfig.baseUrl)).get(
            resultClass = ContentArray::class.java,
            subPath = "content/${pageId}/child/comment",
            parameters = mapOf(
                "limit" to 100,
                "expand" to pageExpands.joinToString(",")
            )
        ).results
    }

    private fun getApiUrl(baseUrl: String) = "${baseUrl}${API_SUFFIX}"

    private fun getHttpClient(bvUser: String, sourceName: String, url: String): BVHttpClient {
        return httpClientFactory.createClient(bvUser = bvUser, sourceName = sourceName, url)
    }
}
