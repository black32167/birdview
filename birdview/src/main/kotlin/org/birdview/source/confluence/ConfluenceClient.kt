package org.birdview.source.confluence

import org.birdview.source.confluence.model.ConfluenceSearchItem
import org.birdview.source.confluence.model.ConfluenceSearchPageResponseSearchResult
import org.birdview.storage.BVConfluenceConfig
import org.birdview.utils.BVTimeUtil
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import java.lang.Integer.min
import javax.ws.rs.client.WebTarget

class ConfluenceClient(val config: BVConfluenceConfig) {
    private val log = LoggerFactory.getLogger(ConfluenceClient::class.java)
    private val documentsPerPage = 50

    fun findDocuments(cql: String?, chunkConsumer: (List<ConfluenceSearchItem>) -> Unit) {
        if (cql == null) {
            return
        }
        log.info("Running cql '{}'", cql)

        var startAt:Int? = 0
        try {
            do {
                BVTimeUtil.logTime("confluence-findDocs-page") {
                    val response = getTarget()
                            .path("search")
                            .queryParam("start", startAt)
                            .queryParam("limit", documentsPerPage)
                            .queryParam("cql", cql)
                            .request()
                            .get()
                            .also(ResponseValidationUtils::validate)
                    val searchResult = response.readEntity(ConfluenceSearchPageResponseSearchResult::class.java)
                    chunkConsumer(searchResult.results)

                    startAt = min(searchResult.start + searchResult.size, searchResult.totalSize)
                            .takeIf { it < searchResult.totalSize }
                }

            } while (startAt != null)
        } catch (e: Exception) {
            log.error("", e)
        }
    }

    private fun getTarget(): WebTarget = getTargetFactory(config.baseUrl).getTarget("/rest/api")

    private fun getTargetFactory(url: String) = WebTargetFactory(url) {
        BasicAuth(config.user, config.token)
    }
}
