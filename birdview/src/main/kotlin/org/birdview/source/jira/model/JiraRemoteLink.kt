package org.birdview.source.jira.model

import com.fasterxml.jackson.annotation.JsonAlias

data class JiraRemoteLink(
        val id: String,
        @JsonAlias("object")
        val _object: JiraRemoteLinkObject
)

class JiraRemoteLinkObject (
    val url: String,
    val title: String
)
