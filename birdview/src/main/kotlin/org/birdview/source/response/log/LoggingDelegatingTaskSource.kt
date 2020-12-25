package org.birdview.source.response.log

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.storage.BVAbstractSourceConfig
import org.slf4j.LoggerFactory

class LoggingDelegatingTaskSource(private val delegate: BVTaskSource) : BVTaskSource {
    private val log = LoggerFactory.getLogger(LoggingDelegatingTaskSource::class.java)

    override fun getTasks(
        bvUser: String,
        updatedPeriod: TimeIntervalFilter,
        sourceConfig: BVAbstractSourceConfig,
        chunkConsumer: (List<BVDocument>) -> Unit
    ) {
        delegate.getTasks(bvUser, updatedPeriod, sourceConfig) { docs ->
            log.info("Loaded {} docs for {}", docs.size, delegate::class.java)
            chunkConsumer(docs)
        }
    }

    override fun getType(): SourceType =
        delegate.getType()

    override fun isAuthenticated(sourceName: String) =
        delegate.isAuthenticated(sourceName)

    override fun canHandleId(id: String) =
        delegate.canHandleId(id)

    override fun loadByIds(sourceName: String, keyList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
        delegate.loadByIds(sourceName, keyList) { docs->
            log.info("Loaded (1) {} docs for {}", docs.size, delegate::class.java)
            chunkConsumer(docs)
        }
    }

    override fun resolveSourceUserId(sourceName: String, email: String): String =
        delegate.resolveSourceUserId(sourceName, email)
}
