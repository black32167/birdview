package org.birdview.source.slack

import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.storage.BVUserSourceConfigStorage
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class SlackTaskService(
    private val slackClient: SlackClient,
    private val userSourceStorage: BVUserSourceConfigStorage,
): BVTaskSource {
    private val log = LoggerFactory.getLogger(SlackTaskService::class.java)
    override fun getTasks(
        bvUser: String,
        updatedPeriod: TimeIntervalFilter,
        sourceConfig: BVSourceConfigProvider.SyntheticSourceConfig,
        chunkConsumer: BVSessionDocumentConsumer
    ) {
        try {
            slackClient.findMessages(sourceConfig, chunkConsumer::consume)
        } catch (e: Exception) {
            log.error("Error reading Slack data (source {})", sourceConfig.sourceName, e)
        }
    }

    override fun getType(): SourceType = SourceType.SLACK
}
