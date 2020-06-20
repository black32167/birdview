package org.birdview.config

import org.birdview.utils.JsonDeserializer
import javax.inject.Named

@Named
class BVUsersConfigProvider(
        private val bvRuntimeConfig: BVRuntimeConfig,
        private val jsonDeserializer: JsonDeserializer
) {
    fun getUserName(userAlias: String, sourceName: String): String =
            getConfig()
                    .find { it.alias == userAlias }
                    ?.sources
                    ?.find { it.sourceName == sourceName }
                    ?.sourceUserName
                    ?: throw RuntimeException("Cannot find user for alias '${userAlias}' and source '${sourceName}'")

    private fun getConfig(): Array<BVUserSourcesConfig> = try {
        jsonDeserializer.deserialize(bvRuntimeConfig.usersConfigFileName)
    } catch (e: Exception) {
        e.printStackTrace()
        arrayOf()
    }
}

class BVUserSourcesConfig(
        val alias: String, // user alias
        val sources: Array<BVUserSourceConfig>
)

class BVUserSourceConfig (
        val sourceName: String,
        val sourceUserName: String
)