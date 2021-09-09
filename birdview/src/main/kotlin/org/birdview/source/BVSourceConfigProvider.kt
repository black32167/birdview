package org.birdview.source

import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.SourceSecretsMapper
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.slf4j.LoggerFactory
import javax.inject.Named

/**
 * Computes synthetic source configuration for user.
 */
@Named
class BVSourceConfigProvider(
    val userSourceConfigStorage: BVUserSourceConfigStorage,
    val oAuthTokenStorage: OAuthTokenStorage,
    val sourceSecretsMapper: SourceSecretsMapper
) {
    class SyntheticSourceConfig(
        val sourceName: String,
        val sourceType: SourceType,
        val baseUrl: String,
        val sourceUserName: String,
        val enabled: Boolean = false,
        val sourceSecret: BVSourceSecret
    )

    private val log = LoggerFactory.getLogger(BVSourceConfigProvider::class.java)

    fun getSourceConfig(sourceName: String, bvUser: String): SyntheticSourceConfig =
        userSourceConfigStorage.getSource(bvUser = bvUser, sourceName = sourceName)
            ?.let(this::createSyntheticConfig)
            ?: throw IllegalArgumentException("Cannot find source ${sourceName} for user ${bvUser}")

    fun listEnabledSourceConfigs(bvUser: String): List<SyntheticSourceConfig> {

        val enabledUserSources: List<BVUserSourceConfig> = userSourceConfigStorage.listSources(bvUser)
            .filter(BVUserSourceConfig::enabled)

        return enabledUserSources
            .mapNotNull (this::createSyntheticConfig)
            .filter { isAuthenticated(bvUser, it) }
    }

    private fun isAuthenticated(bvUser: String, config: SyntheticSourceConfig): Boolean {
        if (config.sourceSecret is BVOAuthSourceSecret) {
            val sourceName = config.sourceName
            val token = oAuthTokenStorage.loadOAuthTokens(bvUser, sourceName)
            if (token == null) {
                log.debug("OAuth token is not set for source {}, user {}", sourceName, bvUser)
                return false
            } else if (token.isExpired() && token.refreshToken == null) {
                log.warn("OAuth token is expired but can't be renewed for source {}, user {}", sourceName, bvUser)
                return false
            }
        }
        return true
    }

    private fun createSyntheticConfig(userSourceConfig: BVUserSourceConfig): SyntheticSourceConfig? = try {
        SyntheticSourceConfig(
            sourceName = userSourceConfig.sourceName,
            sourceType = userSourceConfig.sourceType,
            baseUrl = userSourceConfig.baseUrl,
            sourceUserName = userSourceConfig.sourceUserName,
            sourceSecret = sourceSecretsMapper.deserialize(userSourceConfig.serializedSourceSecret)
        )
    } catch (e: Exception) {
        null
    }
}
