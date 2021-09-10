package org.birdview.source.jira

import org.birdview.model.BVDocumentStatus

object JiraIssueStatusMapper {
    private val docStatus2JiraStatuses = mapOf(
            BVDocumentStatus.DONE to listOf("done"),
            BVDocumentStatus.PROGRESS to listOf("progress", "review", "blocked"),
            BVDocumentStatus.PLANNED to listOf("to do", "todo"),
            BVDocumentStatus.BACKLOG to listOf("backlog")
    )

    fun toJiraStatuses(documentStatus: BVDocumentStatus):List<String>? =
            docStatus2JiraStatuses[documentStatus]

    fun toBVStatus(jiraStatus: String): BVDocumentStatus =
        docStatus2JiraStatuses
                .entries
                .firstOrNull { it.value.any() { jiraStatus.toLowerCase().contains(it) } }
                ?.key
                ?: BVDocumentStatus.PLANNED
}
