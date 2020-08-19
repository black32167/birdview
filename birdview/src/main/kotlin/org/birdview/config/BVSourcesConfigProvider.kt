package org.birdview.config

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.birdview.source.SourceType
import org.birdview.utils.JsonDeserializer
import java.nio.file.Files
import java.nio.file.StandardCopyOption
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
            ?.filter { !it.toString().toLowerCase().endsWith(".bak") }
            ?.collect(Collectors.toList())
            ?.mapNotNull(jsonDeserializer::deserialize)
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

    fun update(config: BVAbstractSourceConfig) {
        bvRuntimeConfig.sourcesConfigsFolder.resolve(config.sourceName).also { file ->
            Files.move(file, file.resolveSibling("${file}.bak"), StandardCopyOption.REPLACE_EXISTING)
            jsonDeserializer.serialize(file, config)
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
        val sourceName: String,
        val user: String
)

abstract class BVOAuthSourceConfig (
        sourceType: SourceType,
        sourceName: String,
        user: String,
        val clientId: String,
        val clientSecret: String,
        val tokenExchangeUrl: String,
        val scope: String)
    : BVAbstractSourceConfig(sourceType, sourceName, user) {
}

class BVJiraConfig (
        sourceName: String = "jira",
        val baseUrl: String,
        user: String,
        val token: String
): BVAbstractSourceConfig (SourceType.JIRA, sourceName, user)

class BVTrelloConfig (
        sourceName: String = "trello",
        user: String,
        val baseUrl: String = "https://api.trello.com",
        val key: String,
        val token: String
): BVAbstractSourceConfig (SourceType.TRELLO, sourceName, user)

class BVGithubConfig (
        sourceName: String = "github",
        val baseUrl: String = "https://api.github.com",
        user: String,
        val token: String
): BVAbstractSourceConfig (SourceType.GITHUB, sourceName, user)

class BVGDriveConfig (
        sourceName: String = "gdrive",
        clientId: String,
        clientSecret: String,
        user: String
): BVOAuthSourceConfig(
        sourceType = SourceType.GDRIVE,
        sourceName = sourceName,
        user = user,
        clientId = clientId,
        clientSecret = clientSecret,
        tokenExchangeUrl = "https://oauth2.googleapis.com/token",
        scope = "https://www.googleapis.com/auth/drive"
)