package org.birdview.source.jira

import org.birdview.model.DocumentStatus

object JiraIssueStatusMapper {
    private val docStatus2JiraStatuses = mapOf(
            DocumentStatus.DONE to listOf("Done"),
            DocumentStatus.PROGRESS to listOf("In Progress", "In Review", "Blocked"),
            DocumentStatus.PLANNED to listOf("To Do"),
            DocumentStatus.BACKLOG to listOf("Backlog")
    )

    fun toJiraStatuses(documentStatus: DocumentStatus):List<String>? =
            docStatus2JiraStatuses[documentStatus]

    fun toBVStatus(jiraStatus: String): DocumentStatus? =
        docStatus2JiraStatuses
                .entries
                .firstOrNull{ it.value.contains(jiraStatus) }
                ?.key
}
