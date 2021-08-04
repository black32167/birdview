package org.birdview.storage.model.secrets

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.birdview.source.SourceType

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