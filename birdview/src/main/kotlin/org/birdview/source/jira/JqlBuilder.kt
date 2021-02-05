package org.birdview.source.jira

import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVJiraConfig
import org.birdview.storage.BVUserSourceStorage
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class JqlBuilder(
        private val userSourceStorage: BVUserSourceStorage
) {
    fun getJql(user: String, updatedPeriod: TimeIntervalFilter, jiraConfig: BVJiraConfig): String? =
            getUserJqlClause(user, jiraConfig)
                    .let { userClause ->
                        listOfNotNull(
                                userClause,
                                getIssueUpdateAfterClause(updatedPeriod.after),
                                getIssueUpdateBeforeClause(updatedPeriod.before)
                        ).joinToString(" AND ") + " ORDER BY updatedDate DESC"
                    }

    private fun getIssueUpdateAfterClause(after: OffsetDateTime?): String? =
            after?.let {  "updatedDate > \"${formatDate(it)}\" " }

    private fun getIssueUpdateBeforeClause(before: OffsetDateTime?): String? =
            before?.let {  "updatedDate <= \"${formatDate(it)}\" " }

    private fun formatDate(date: OffsetDateTime) =
            date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    private fun getUserJqlClause(bvUser: String, jiraConfig: BVJiraConfig): String {
        val user = "\"${getUser(bvUser, jiraConfig)}\""
        return "(creator = $user OR assignee = $user OR watcher = $user)"
    }

    private fun getUser(userAlias: String, jiraConfig: BVJiraConfig): String =
            userSourceStorage.getSourceProfile(userAlias, jiraConfig.sourceName).sourceUserName
}