package org.birdview.config

import org.birdview.utils.JsonDeserializer
import org.springframework.cache.annotation.Cacheable
import java.nio.file.Files
import javax.inject.Named

@Named
open class BVUsersConfigProvider(
        private val bvRuntimeConfig: BVRuntimeConfig,
        private val jsonDeserializer: JsonDeserializer,
        private val bvSourcesConfigProvider: BVSourcesConfigProvider
) {
    fun getUserName(userAlias: String?, sourceName: String): String =
            if (userAlias.isNullOrBlank()) {
                bvSourcesConfigProvider.getConfigByName(sourceName)?.user ?: throw java.lang.RuntimeException("Config not found for $sourceName")
            } else {
                getConfig()
                        .find { it.alias == userAlias }
                        ?.sources
                        ?.find { it.sourceName == sourceName }
                        ?.sourceUserName
                        ?: throw RuntimeException("Cannot find user for alias '${userAlias}' and source '${sourceName}'")
            }

    @Cacheable
    private fun getConfig(): Array<BVUserSourcesConfig> =
            bvRuntimeConfig.usersConfigFileName
                    .takeIf { Files.exists(it) }
                    ?.let (jsonDeserializer::deserialize)
                    ?: emptyArray()


    fun listUsers() = getConfig()
            .map { it.alias }
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