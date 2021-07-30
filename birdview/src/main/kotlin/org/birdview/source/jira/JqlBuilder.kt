package org.birdview.source.jira

import org.birdview.model.TimeIntervalFilter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class JqlBuilder {
    fun getJql(sourceUserName: String, updatedPeriod: TimeIntervalFilter): String =
            getUserJqlClause(sourceUserName)
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

    private fun getUserJqlClause(sourceUserName: String): String {
        val user = "\"${sourceUserName}\""
        return "(creator = $user OR assignee = $user OR watcher = $user)"
    }
}