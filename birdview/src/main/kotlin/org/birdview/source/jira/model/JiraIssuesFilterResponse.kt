package org.birdview.source.jira.model

class JiraIssuesFilterResponse (
        val issues: Array<JiraIssue>,
        val startAt: Int,
        val total: Int,
        val maxResults: Int
)

class JiraIssue (
    val key: String,
    val self: String,
    val fields : JiraIssueFields,
    val changelog: JiraChangelog?
)

class JiraWatcher(
        val self: String
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
        val self: String?,
        val displayName: String?,
        val emailAddress: String?
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
    val parent: JiraParentIssue?,
    val watches: JiraWatcher?,
    val creator: JiraUser?,
    val reporter: JiraUser?,
    val assignee: JiraUser?
)

class JiraIssueStatus(val name:String)

class JiraParentIssue (
    val key: String,
    val fields: JiraParentIssueFields
)

class JiraParentIssueFields (
    val summary: String
)
