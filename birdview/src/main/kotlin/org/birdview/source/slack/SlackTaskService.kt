package org.birdview.source.slack

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVSlackConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class SlackTaskService(
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val tokenStorage: OAuthRefreshTokenStorage,
        private val slackClient: SlackClient
): BVTaskSource {
    private val log = LoggerFactory.getLogger(SlackTaskService::class.java)
    override fun getTasks(
            bvUser: String,
            updatedPeriod: TimeIntervalFilter,
            sourceConfig: BVAbstractSourceConfig,
            chunkConsumer: (List<BVDocument>) -> Unit) {
        val slackConfig = sourceConfig as BVSlackConfig
        try {
            slackClient.findMessages(slackConfig, chunkConsumer)
        } catch (e: Exception) {
            log.error("Error reading Slack data (source {})", sourceConfig.sourceName, e)
        }
    }

    override fun getType(): SourceType = SourceType.SLACK

    override fun isAuthenticated(sourceName: String): Boolean =
            sourceSecretsStorage.getConfigByName(sourceName, BVSlackConfig::class.java)
                    ?.let { config -> tokenStorage.hasToken(config) }
                    ?: false
}
