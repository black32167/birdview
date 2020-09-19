package org.birdview.source.slack

import org.birdview.analysis.BVDocument
import org.birdview.config.BVSlackConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class SlackTaskService(
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        private val tokenStorage: OAuthRefreshTokenStorage
): BVTaskSource {
    private val log = LoggerFactory.getLogger(SlackTaskService::class.java)
    override fun getTasks(user: String?, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit) {
        sourcesConfigProvider.getConfigsOfType(BVSlackConfig::class.java)
                .forEach { config -> getTasks(user, updatedPeriod, config, chunkConsumer) }
    }

    fun getTasks(
            user: String?,
            updatedPeriod: TimeIntervalFilter,
            sourceConfig: BVSlackConfig,
            chunkConsumer: (List<BVDocument>) -> Unit) {
        try {
            val client = SlackClient(sourceConfig, tokenStorage)
            client.findMessages(sourceConfig, chunkConsumer)
        } catch (e: Exception) {
            log.error("Error reading Slack data (source {})", sourceConfig.sourceName, e)
        }
    }

    override fun getType(): SourceType = SourceType.SLACK

    override fun isAuthenticated(sourceName: String): Boolean =
            sourcesConfigProvider.getConfigByName(sourceName, BVSlackConfig::class.java)
                    ?.let { config -> tokenStorage.hasToken(config) }
                    ?: false
}
