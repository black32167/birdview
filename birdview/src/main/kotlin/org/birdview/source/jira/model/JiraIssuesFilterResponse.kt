package org.birdview.source.jira.model

class JiraIssuesFilterResponse (
        val issues: Array<JiraIssue>
)

class JiraIssue (
    val key: String,
    val self: String,
    val fields : JiraIssueFields,
    val changelog: JiraChangelog?
)

class JiraChangelog (
    val histories: List<JiraChangelogItem>
)

class JiraChangelogItem (
        val author: JiraUser,
        val items: List<JiraHistoryItem>,
        val created: String
)

class JiraUser (
        val emailAddress: String
)

class JiraHistoryItem (
    val field: String
)

class JiraIssueFields (
    val updated: String,
    val created: String,
    val summary: String,
    val status: JiraIssueStatus,
    val description: String?,
    val customfield_10007: String?, //EPIC key
    val parent: JiraParentIssue?
)

class JiraIssueStatus(val name:String)

class JiraParentIssue (
    val key: String,
    val fields: JiraParentIssueFields
)

class JiraParentIssueFields (
    val summary: String
)
