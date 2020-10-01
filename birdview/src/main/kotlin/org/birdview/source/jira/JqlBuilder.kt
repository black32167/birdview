package org.birdview.source.jira

import org.birdview.config.sources.BVJiraConfig
import org.birdview.config.user.BVUserProfileStorage
import org.birdview.model.TimeIntervalFilter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class JqlBuilder(
        private val userProfileStorage: BVUserProfileStorage
) {
    fun getJql(user: String?, updatedPeriod: TimeIntervalFilter, jiraConfig: BVJiraConfig): String? =
            getUserJqlClause(user, jiraConfig)
                    .let { userClause ->
                        listOfNotNull(
                                userClause,
                                getIssueUpdateAfterClause(updatedPeriod.after),
                                getIssueUpdateBeforeClause(updatedPeriod.before)
                        ).joinToString(" and ") + " order by updatedDate DESC"
                    }

    private fun getIssueUpdateAfterClause(after: ZonedDateTime?): String? =
            after?.let {  "updatedDate >= \"${formatDate(it)}\" " }

    private fun getIssueUpdateBeforeClause(before: ZonedDateTime?): String? =
            before?.let {  "updatedDate < \"${formatDate(it)}\" " }

    private fun formatDate(date: ZonedDateTime) =
            date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    private fun getUserJqlClause(user: String?, jiraConfig: BVJiraConfig): String {
        val user = getUser(user, jiraConfig)
        return "(creator = $user OR assignee = $user OR watcher = $user)"
    }

    private fun getUser(userAlias: String?, jiraConfig: BVJiraConfig): String =
            if(userAlias == null) { "currentUser()" }
            else { "\"${userProfileStorage.getUserName(userAlias, jiraConfig.sourceName)}\""}
}