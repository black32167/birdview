package org.birdview.config

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.birdview.source.SourceType
import org.birdview.utils.JsonDeserializer
import java.nio.file.Files
import java.util.stream.Collectors
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

    private fun getSourceConfigs(): List<BVAbstractSourceConfig> = bvRuntimeConfig.sourcesConfigsFolder
            .takeIf { Files.isDirectory(it) }
            ?.let (Files::list)
            ?.collect(Collectors.toList())
            ?.map(jsonDeserializer::deserialize)
            ?: emptyList()

    fun getConfigByName(sourceName: String): BVAbstractSourceConfig? =
        getSourceConfigs().find { it.sourceName == sourceName }

    fun <T: BVAbstractSourceConfig> getConfigByName(sourceName: String, configClass: Class<T>): T? =
            getConfigByName(sourceName) as? T

    fun listSourceNames(): List<String> =
        getSourceConfigs().map { it.sourceName }

    fun save(config: BVAbstractSourceConfig) {
        bvRuntimeConfig.sourcesConfigsFolder.also { folder->
            Files.createDirectories(folder)
            jsonDeserializer.serialize(folder.resolve(config.sourceName), config)
        }
    }

    fun delete(sourceName: String) {
        Files.delete(bvRuntimeConfig.sourcesConfigsFolder.resolve(sourceName))
    }
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
        val sourceType: SourceType,
        val sourceName: String
)

abstract class BVOAuthSourceConfig (
        sourceType: SourceType,
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
): BVAbstractSourceConfig (SourceType.JIRA, sourceName)

class BVTrelloConfig (
        sourceName: String = "trello",
        val baseUrl: String = "https://api.trello.com",
        val key: String,
        val token: String
): BVAbstractSourceConfig (SourceType.TRELLO, sourceName)

class BVGithubConfig (
        sourceName: String = "github",
        val baseUrl: String = "https://api.github.com",
        val user: String,
        val token: String
): BVAbstractSourceConfig (SourceType.GITHUB, sourceName)

class BVGDriveConfig (
        sourceName: String = "gdrive",
        clientId: String,
        clientSecret: String
): BVOAuthSourceConfig(
        sourceType = SourceType.GDRIVE,
        sourceName = sourceName,
        clientId = clientId,
        clientSecret = clientSecret,
        tokenExchangeUrl = "https://oauth2.googleapis.com/token",
        scope = "https://www.googleapis.com/auth/drive"
)