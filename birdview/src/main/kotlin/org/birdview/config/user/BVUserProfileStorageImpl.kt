package org.birdview.config.user

import org.birdview.config.BVRuntimeConfig
import org.birdview.config.sources.BVSourcesConfigStorage
import org.birdview.utils.JsonDeserializer
import java.nio.file.Files
import javax.inject.Named

@Named
class BVUserProfileStorageImpl(
        private val bvRuntimeConfig: BVRuntimeConfig,
        private val jsonDeserializer: JsonDeserializer,
        private val sourcesConfigStorage: BVSourcesConfigStorage
): BVUserProfileStorage {
    override fun getUserName(userAlias: String?, sourceName: String): String =
            if (userAlias.isNullOrBlank()) {
                sourcesConfigStorage.getConfigByName(sourceName)?.user ?: throw java.lang.RuntimeException("Config not found for $sourceName")
            } else {
                getConfig()
                        .find { it.alias == userAlias }
                        ?.sources
                        ?.find { it.sourceName == sourceName }
                        ?.sourceUserName
                        ?: throw RuntimeException("Cannot find user for alias '${userAlias}' and source '${sourceName}'")
            }

    override fun listUsers() = getConfig().map { it.alias }

    private fun getConfig(): Array<BVUserSourcesConfig> =
            bvRuntimeConfig.usersConfigFileName
                    .takeIf { Files.exists(it) }
                    ?.let (jsonDeserializer::deserialize)
                    ?: emptyArray()
}

class BVUserSourcesConfig(
        val alias: String, // user alias
        val sources: Array<BVUserSourceConfig>,
        val default: Boolean = false
)

class BVUserSourceConfig (
        val sourceName: String,
        val sourceUserName: String
)