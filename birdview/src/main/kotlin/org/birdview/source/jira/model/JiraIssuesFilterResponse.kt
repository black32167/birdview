package org.birdview.source.jira.model

data class JiraIssuesFilterResponse (
        val issues: List<JiraIssue>,
        val startAt: Int,
        val total: Int,
        val maxResults: Int,
        val isLast: Boolean
)

data class JiraIssue (
    val key: String,
    val self: String,
    val fields : JiraIssueFields,
    val changelog: JiraChangelog?
)

data class JiraWatcher(
        val self: String
)

data class JiraChangelog (
    val histories: List<JiraChangelogItem>
)

data class JiraChangelogItem (
        val author: JiraUser,
        val items: List<JiraHistoryItem>,
        val created: String
)

data class JiraUser (
        val self: String?,
        val displayName: String?,
        val emailAddress: String?
)

data class JiraHistoryItem (
    val field: String
)

data class JiraIssueFields (
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
    val assignee: JiraUser?,
    val priority: JiraIssuePriority?,
    val issuelinks: List<JiraIssueLink>?
)

data class JiraIssueLink(
        val id: String,
        val type: JiraIssueLinkType,
        val outwardIssue: JiraLinkedIssue?
)

data class JiraLinkedIssue (
        val id: String,
        val key: String,
        val self: String
)

data class JiraIssueLinkType(
        val id: String,
        val name: String,
        val inward: String,
        val outward: String
)

data class JiraIssuePriority (
        val id: String,
        val name: String
)
data class JiraIssueStatus(val name:String)

data class JiraParentIssue (
    val key: String,
    val fields: JiraParentIssueFields
)

data class JiraParentIssueFields (
    val summary: String
)
