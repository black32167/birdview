package org.birdview.config

import org.birdview.analysis.BVDocumentUser
import org.birdview.model.UserRole
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
            if ("" == sourceUser) {
                bvSourcesConfigProvider.getConfigByName(sourceName)?.user
            } else {
                getConfig()
                        .find { config ->
                            config.sources.any { source -> source.sourceUserName == sourceUser }
                        }
                        ?.alias
            }

    @Cacheable
    private fun getConfig(): Array<BVUserSourcesConfig> =
            bvRuntimeConfig.usersConfigFileName
                    .takeIf { Files.exists(it) }
                    ?.let (jsonDeserializer::deserialize)
                    ?: emptyArray()

    fun getUser(sourceUserName: String?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            getUserAlias(sourceUserName, sourceName)
                    ?.let { alias -> BVDocumentUser(alias, userRole) }

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