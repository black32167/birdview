package org.birdview.config

import org.birdview.utils.JsonDeserializer
import org.springframework.cache.annotation.Cacheable
import javax.inject.Named

@Named
open class BVUsersConfigProvider(
        private val bvRuntimeConfig: BVRuntimeConfig,
        private val jsonDeserializer: JsonDeserializer
) {

    fun getDefaultUserAlias(): String =
            getConfig()
                    .find { it.default }
                    ?.alias
                    ?: throw IllegalStateException("Cannot find default user configuration")

    fun getUserName(userAlias: String, sourceName: String): String =
            getConfig()
                    .find { it.alias == userAlias }
                    ?.sources
                    ?.find { it.sourceName == sourceName }
                    ?.sourceUserName
                    ?: throw RuntimeException("Cannot find user for alias '${userAlias}' and source '${sourceName}'")

    fun getUserAlias(sourceUser: String?, sourceName: String): String? =
            getConfig()
                    .find { config ->
                        config.sources.any { source->source.sourceUserName == sourceUser }
                    }
                    ?.alias

    @Cacheable
    private fun getConfig(): Array<BVUserSourcesConfig> = try {
        jsonDeserializer.deserialize(bvRuntimeConfig.usersConfigFileName)
    } catch (e: Exception) {
        e.printStackTrace()
        arrayOf()
    }


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