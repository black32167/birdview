package org.birdview.source.jira

import org.birdview.model.BVDocumentStatus

object JiraIssueStatusMapper {
    private val docStatus2JiraStatuses = mapOf(
            BVDocumentStatus.DONE to listOf("Done"),
            BVDocumentStatus.PROGRESS to listOf("In Progress", "In Review", "Blocked"),
            BVDocumentStatus.PLANNED to listOf("To Do"),
            BVDocumentStatus.BACKLOG to listOf("Backlog")
    )

    fun toJiraStatuses(documentStatus: BVDocumentStatus):List<String>? =
            docStatus2JiraStatuses[documentStatus]

    fun toBVStatus(jiraStatus: String): BVDocumentStatus =
        docStatus2JiraStatuses
                .entries
                .firstOrNull{ it.value.contains(jiraStatus) }
                ?.key
                ?: BVDocumentStatus.PLANNED
}
