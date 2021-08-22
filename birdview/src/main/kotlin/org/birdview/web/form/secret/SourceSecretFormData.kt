package org.birdview.web.form.secret

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.birdview.source.SourceTypeNames

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "sourceType")
@JsonSubTypes(
    JsonSubTypes.Type(value = ConfluenceSourceSecretFormData::class, name = SourceTypeNames.SLACK),
    JsonSubTypes.Type(value = SlackSourceSecretFormData::class, name = SourceTypeNames.SLACK),
    JsonSubTypes.Type(value = GdriveSourceSecretFormData::class, name = SourceTypeNames.GDRIVE),
    JsonSubTypes.Type(value = GithubSourceSecretFormData::class, name = SourceTypeNames.GITHUB),
    JsonSubTypes.Type(value = JiraSourceSecretFormData::class, name = SourceTypeNames.JIRA),
    JsonSubTypes.Type(value = TrelloSourceSecretFormData::class, name = SourceTypeNames.TRELLO),
)
abstract class SourceSecretFormData