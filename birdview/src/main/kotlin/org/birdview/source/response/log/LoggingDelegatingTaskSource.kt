package org.birdview.source.response.log

import com.fasterxml.jackson.databind.ObjectMapper
import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.response.log.model.SourceInteraction
import org.birdview.source.response.log.model.TimeIntervalSourceRequest
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.utils.BVDateTimeUtils
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.util.*

class LoggingDelegatingTaskSource(private val delegate: BVTaskSource) : BVTaskSource {
    private val log = LoggerFactory.getLogger(LoggingDelegatingTaskSource::class.java)
    private val objectMapper = ObjectMapper().writerWithDefaultPrettyPrinter()

    override fun getTasks(
        bvUser: String,
        updatedPeriod: TimeIntervalFilter,
        sourceConfig: BVAbstractSourceConfig,
        chunkConsumer: (List<BVDocument>) -> Unit
    ) {
        delegate.getTasks(bvUser, updatedPeriod, sourceConfig) { docs ->
            log.info("Loaded {} docs for {}", docs.size, delegate::class.java)
            serialize(SourceInteraction(
                sourceName = sourceConfig.sourceName,
                sourceType = sourceConfig.sourceType,
                request = TimeIntervalSourceRequest(
                    bvUser = bvUser,
                    fromTime = toString(updatedPeriod.after),
                    toTime = toString(updatedPeriod.before)),
                response = docs))
            chunkConsumer(docs)
        }
    }

    private fun toString(time: ZonedDateTime?): String? =
        time?.let { BVDateTimeUtils.dateTimeFormat(it) }

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

    private fun serialize(interaction: SourceInteraction) {
        objectMapper.writeValue(getOS(sourceImpl=delegate::class.java.simpleName), interaction)
    }

    private fun getOS(sourceImpl:String):OutputStream =
        Files.newOutputStream(
            Files.createDirectories(Paths.get("/tmp/birdview/response"))
                .resolve("${sourceImpl}-${UUID.randomUUID()}.json"))
}
