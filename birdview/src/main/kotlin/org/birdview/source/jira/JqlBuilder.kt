package org.birdview.source.jira

import org.birdview.config.BVJiraConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class JqlBuilder(
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getJql(filter: BVDocumentFilter, jiraConfig: BVJiraConfig): String? {
        val userClause = getUserJqlClause(filter.userFilters, jiraConfig)
        return if (userClause.isBlank()) {
            null
        } else {
            listOfNotNull(
                    userClause,
                    getIssueStatusJqlClause(filter.reportType),
                    getIssueUpdateDateJqlClause(filter.since)
            ).joinToString(" and ") + " order by lastViewed DESC"
        }
    }

    private fun getIssueUpdateDateJqlClause(since: ZonedDateTime?): String? =
            since?.let {  "updatedDate >= \"${it.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))}\" " }

    private fun getIssueStatusJqlClause(reportType: ReportType): String? = when(reportType) {
        ReportType.WORKED -> listOf(DocumentStatus.PROGRESS, DocumentStatus.DONE)
        ReportType.PLANNED -> listOf(DocumentStatus.PLANNED)
        ReportType.LAST_DAY -> listOf(DocumentStatus.DONE)
    }
            .mapNotNull (JiraIssueStatusMapper::toJiraStatuses)
            .flatten()
            .joinToString (  ",", transform = { "\"${it}\"" })
            .let { statuses -> "status in (${statuses})" }

    private fun getUserJqlClause(userFilters: List<UserFilter>, jiraConfig: BVJiraConfig): String = userFilters
            .joinToString(" or ", "(", ")", transform = {
                filter -> getUserJqlClause(filter, jiraConfig)})

    private fun getUserJqlClause(userFilter: UserFilter, jiraConfig: BVJiraConfig): String = when (userFilter.role) {
        UserRole.CREATOR -> "creator"
        UserRole.IMPLEMENTOR -> "assignee"
        UserRole.WATCHER -> "watcher"
    } + " = ${getUser(userFilter.userAlias, jiraConfig)}"


    private fun getUser(userAlias: String?, jiraConfig: BVJiraConfig): String =
            if(userAlias == null) { "currentUser()" }
            else { "\"${usersConfigProvider.getUserName(userAlias, jiraConfig.sourceName)}\""}
}