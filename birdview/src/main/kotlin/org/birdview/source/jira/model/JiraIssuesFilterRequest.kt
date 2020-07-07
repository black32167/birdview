package org.birdview.source.jira.model

class JiraIssuesFilterRequest (
    val maxResults:Int,
    val jql: String,
    val fields: Array<String>,
    val validateQuery: Boolean = false
)