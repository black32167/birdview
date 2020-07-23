package org.birdview.source.jira.model

data class JiraIssuesFilterRequest (
    val startAt:Int,
    val maxResults:Int,
    val jql: String,
    val fields: Array<String>,
    val validateQuery: Boolean = false
)