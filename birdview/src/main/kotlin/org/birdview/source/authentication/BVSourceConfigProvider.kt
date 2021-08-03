package org.birdview.source.authentication

import org.birdview.storage.*
import org.birdview.storage.model.BVUserSourceConfig
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class BVSourceConfigProvider(
    val defaultSourceConfigStorage:BVSourceSecretsStorage,
    val userSourceConfigStorage:BVUserSourceStorage,
    val oAuthTokenStorage: OAuthTokenStorage
) {
    private val log = LoggerFactory.getLogger(BVSourceConfigProvider::class.java)

    fun <T:BVAbstractSourceConfig> getSourceConfig(sourceName:String, bvUser:String, configClass: Class<T>): T? {
        val sourceConfig = userSourceConfigStorage.getSourceProfile(bvUser = bvUser, sourceName = sourceName).sourceConfig
            ?: defaultSourceConfigStorage.getConfigByName(sourceName)
        return sourceConfig?.let(configClass::cast)
    }

    fun listEnabledSourceConfigs(bvUser:String):List<BVAbstractSourceConfig> {
        val userSourceProfiles:List<BVUserSourceConfig> = userSourceConfigStorage.listUserSourceProfiles(bvUser)
            .filter (BVUserSourceConfig::enabled)

        val configuredUserSourceConfigs:List<BVAbstractSourceConfig> = userSourceProfiles
            .mapNotNull {
                it.sourceConfig ?: defaultSourceConfigStorage.getConfigByName(it.sourceName)
            }
        return configuredUserSourceConfigs.filter { isAuthenticated(it) }
    }

    private fun isAuthenticated(config: BVAbstractSourceConfig): Boolean {
        if (config is BVOAuthSourceConfig) {
            val sourceName = config.sourceName
            val token = oAuthTokenStorage.loadOAuthTokens(sourceName)
            if (token == null) {
                log.debug("OAuth token is not set for source {}", sourceName)
                return false
            } else if (token.isExpired() && token.refreshToken == null) {
                log.warn("OAuth token is expired but can't be renewed for source {}", sourceName)
                return false
            }
        }
        return true
    }
}
