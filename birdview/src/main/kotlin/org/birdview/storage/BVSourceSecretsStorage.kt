package org.birdview.storage

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.birdview.source.SourceType

interface BVSourceSecretsStorage {

    fun <T: BVAbstractSourceConfig> getConfigsOfType(configClass: Class<T>):List<T>

    fun <T: BVAbstractSourceConfig> getConfigOfType(configClass: Class<T>): T?

    fun getConfigByName(sourceName: String): BVAbstractSourceConfig?

    fun <T: BVAbstractSourceConfig> getConfigByName(sourceName: String, configClass: Class<T>) : T?

    fun listSourceNames(): List<String>

    fun create(config: BVAbstractSourceConfig)

    fun update(config: BVAbstractSourceConfig)

    fun delete(sourceName: String)
}

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_sourceType")
@JsonSubTypes(
    JsonSubTypes.Type(value = BVJiraConfig::class, name = "jira"),
    JsonSubTypes.Type(value = BVTrelloConfig::class, name = "trello"),
    JsonSubTypes.Type(value = BVGithubConfig::class, name = "github"),
    JsonSubTypes.Type(value = BVGDriveConfig::class, name = "gdrive"),
    JsonSubTypes.Type(value = BVSlackConfig::class, name = "slack"),
    JsonSubTypes.Type(value = BVConfluenceConfig::class, name = "confluence")
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
        val authCodeUrl: String,
        val tokenExchangeUrl: String,
        val scope: String)
    : BVAbstractSourceConfig(sourceType, sourceName, user)

class BVJiraConfig (
        sourceName: String = "jira",
        val baseUrl: String,
        user: String,
        val token: String
): BVAbstractSourceConfig(SourceType.JIRA, sourceName, user)

class BVConfluenceConfig (
        sourceName: String = "confluence",
        val baseUrl: String,
        user: String,
        val token: String
): BVAbstractSourceConfig(SourceType.CONFLUENCE, sourceName, user)

class BVTrelloConfig (
        sourceName: String = "trello",
        user: String,
        val key: String,
        val token: String
): BVAbstractSourceConfig(SourceType.TRELLO, sourceName, user) {
    val baseUrl = "https://api.trello.com"
}

class BVGithubConfig (
        sourceName: String = "github",
        user: String,
        val token: String
): BVAbstractSourceConfig(SourceType.GITHUB, sourceName, user) {
    val baseGqlUrl = "https://api.github.com/graphql"
    val baseUrl = "https://api.github.com"
}

class BVSlackConfig (
        sourceName: String = "slack",
        user: String,
        clientId: String,
        clientSecret: String
): BVOAuthSourceConfig(
        sourceType = SourceType.SLACK,
        sourceName = sourceName,
        user = user,
        clientId = clientId,
        clientSecret = clientSecret,
        authCodeUrl = "https://slack.com/oauth/v2/authorize?user_scope=identity.basic&", //search:read
        tokenExchangeUrl = "https://slack.com/api/oauth.v2.access",
        scope = "channels:history,channels:read"
) {
    val baseUrl = "https://slack.com/api"
}

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
        authCodeUrl = "https://accounts.google.com/o/oauth2/v2/auth?",
        tokenExchangeUrl = "https://oauth2.googleapis.com/token",
        scope = "https://www.googleapis.com/auth/drive"
)