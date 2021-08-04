package org.birdview.source.authentication

import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserSourceSecretsStorage
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.model.BVUserSourceConfig
import org.birdview.storage.model.secrets.BVAbstractSourceConfig
import org.birdview.storage.model.secrets.BVOAuthSourceConfig
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class BVSourceConfigProvider(
    val defaultSourceSecretsStorage:BVSourceSecretsStorage,
    val userSourceSecretsStorage: BVUserSourceSecretsStorage,
    val userSourceConfigStorage:BVUserSourceStorage,
    val oAuthTokenStorage: OAuthTokenStorage
) {
    private val log = LoggerFactory.getLogger(BVSourceConfigProvider::class.java)

    fun <T:BVAbstractSourceConfig> getSourceConfig(sourceName:String, bvUser:String, configClass: Class<T>): T? {
        val sourceConfig = userSourceSecretsStorage.getSecret(bvUser = bvUser, sourceName = sourceName)
            ?: defaultSourceSecretsStorage.getSecret(sourceName)
        return sourceConfig?.let(configClass::cast)
    }

    fun listEnabledSourceConfigs(bvUser:String):List<BVAbstractSourceConfig> {
        val enabledUserSourceNames:MutableSet<String> = userSourceConfigStorage.listUserSourceProfiles(bvUser)
            .filter (BVUserSourceConfig::enabled)
            .map { it.sourceName }
            .toMutableSet()

        val configuredUserSourceConfigs:List<BVAbstractSourceConfig> =
            userSourceSecretsStorage.getSecrets(bvUser)
                .filter { enabledUserSourceNames.contains(it.sourceName) }

        defaultSourceSecretsStorage.getSecrets()
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
