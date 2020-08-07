package org.birdview.config

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.birdview.utils.JsonDeserializer
import javax.inject.Named

@Named
class BVSourcesConfigProvider(
        private val bvRuntimeConfig: BVRuntimeConfig,
        private val jsonDeserializer: JsonDeserializer
) {
    fun <T: BVAbstractSourceConfig> getConfigsOfType(configClass: Class<T>):List<T> =
            getSourceConfigs()
                    .filter { configClass.isAssignableFrom(it.javaClass) }
                    .map { configClass.cast(it) }
                    .toList()

    fun <T: BVAbstractSourceConfig> getConfigOfType(configClass: Class<T>): T? =
            getConfigsOfType(configClass).firstOrNull()

    private fun getSourceConfigs(): Array<BVAbstractSourceConfig>
            = jsonDeserializer.deserialize(bvRuntimeConfig.sourcesConfigFileName)

    fun getConfigByName(sourceName: String): BVAbstractSourceConfig? =
        getSourceConfigs().find { it.sourceName == sourceName }

    fun listSourceNames(): List<String> =
        getSourceConfigs().map { it.sourceName }
}

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "sourceType")
@JsonSubTypes(
    JsonSubTypes.Type(value = BVJiraConfig::class, name = "jira"),
    JsonSubTypes.Type(value = BVTrelloConfig::class, name = "trello"),
    JsonSubTypes.Type(value = BVGithubConfig::class, name = "github"),
    JsonSubTypes.Type(value = BVGDriveConfig::class, name = "gdrive")
)
abstract class BVAbstractSourceConfig (
        val sourceType: String,
        val sourceName: String
)

abstract class BVOAuthSourceConfig (
        sourceType: String,
        sourceName: String,
        val clientId: String,
        val clientSecret: String,
        val tokenExchangeUrl: String,
        val scope: String)
    : BVAbstractSourceConfig(sourceType, sourceName) {
}

class BVJiraConfig (
        sourceName: String = "jira",
        val baseUrl: String,
        val user: String,
        val token: String
): BVAbstractSourceConfig("jira", sourceName)

class BVTrelloConfig (
        sourceName: String = "trello",
        val baseUrl: String,
        val key: String,
        val token: String
): BVAbstractSourceConfig("trello", sourceName)

class BVGithubConfig (
        sourceName: String = "github",
        val baseUrl: String,
        val user: String,
        val token: String
): BVAbstractSourceConfig("github", sourceName)

class BVGDriveConfig (
        sourceName: String = "gdrive",
        clientId: String,
        clientSecret: String
): BVOAuthSourceConfig(
        sourceType = "gdrive",
        sourceName = sourceName,
        clientId = clientId,
        clientSecret = clientSecret,
        tokenExchangeUrl = "https://oauth2.googleapis.com/token",
        scope = "https://www.googleapis.com/auth/drive"
)