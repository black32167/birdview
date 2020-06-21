package org.birdview.source.jira

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.tokenize.TextTokenizer
import org.birdview.config.BVJiraConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.request.TasksRequest
import org.birdview.source.BVTaskSource
import org.birdview.source.jira.model.JiraChangelogItem
import org.birdview.source.jira.model.JiraIssue
import org.birdview.utils.BVFilters
import javax.inject.Named

@Named
class JiraTaskService(
        private val jiraClientProvider: JiraClientProvider,
        private val tokenizer: TextTokenizer,
        sourcesConfigProvider: BVSourcesConfigProvider
): BVTaskSource {
    companion object {
        val JIRA_KEY_TYPE = "jiraKey"
    }
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private val jiraConfigs: List<BVJiraConfig> = sourcesConfigProvider.getConfigsOfType(BVJiraConfig::class.java)

    // TODO: parallelize
    override fun getTasks(request: TasksRequest): List<BVDocument> =
        jiraConfigs.flatMap { config -> getTasks(request, config) }

    private fun getTasks(request: TasksRequest, config: BVJiraConfig): List<BVDocument> {
        val jiraIssues = jiraClientProvider.getJiraClient(config).findIssues(JiraIssuesFilter(
                request.user,
                getIssueStatus(request.status),
                request.since))

        val tasks = jiraIssues.map { issue ->
            val description = issue.fields.description ?: ""
            BVDocument(
                    ids = setOf(BVDocumentId( id = issue.key, type = JIRA_KEY_TYPE, sourceName = config.sourceName)),
                    title = issue.fields.summary,
                    updated = dateTimeFormat.parse(issue.fields.updated),
                    created = dateTimeFormat.parse(issue.fields.created),
                    httpUrl = "${config.baseUrl}/browse/${issue.key}",
                    body = description,
                    refsIds = BVFilters.filterIdsFromText("${description} ${issue.fields.summary}"),
                    groupIds = extractGroupIds(issue, config.sourceName),
                    status = issue.fields.status.name,
                    operations = extractOperations(issue, config)
            )
        }
                .filter { it.operations.isNotEmpty() }
        return tasks
    }

    private fun extractOperations(issue: JiraIssue, config: BVJiraConfig): List<BVDocumentOperation> =
        issue.changelog
                ?.histories
                ?.flatMap (this::toOperation)
                ?.filter { it.author.equals(config.user) }
                ?: emptyList()

    private fun toOperation(changelogItem: JiraChangelogItem): List<BVDocumentOperation> =
            changelogItem.items.map { historyItem ->
                BVDocumentOperation(
                        author = changelogItem.author.emailAddress,
                        field = historyItem.field,
                        created = dateTimeFormat.parse(changelogItem.created)
                )
            }


    override fun getType() = "jira"

    private fun extractGroupIds(issue: JiraIssue, sourceName: String): Set<BVDocumentId> =
            (issue.fields.customfield_10007?.let { setOf(BVDocumentId(it, JIRA_KEY_TYPE, sourceName)) } ?: emptySet<BVDocumentId>()) +
                    (issue.fields.parent?.let{ setOf(BVDocumentId(it.key, JIRA_KEY_TYPE, sourceName)) } ?: emptySet<BVDocumentId>())

    private fun getIssueStatus(status: String): String? = when (status) {
        "done" -> "Done"
        "progress" -> "In Progress"
        "planned" -> "To Do"
        "backlog" -> "Backlog"
        "blocked" -> "Blocked"
        else -> null
    }
}