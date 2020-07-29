package org.birdview.source.jira

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentUser
import org.birdview.config.BVJiraConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVTaskSource
import org.birdview.source.jira.model.JiraChangelogItem
import org.birdview.source.jira.model.JiraIssue
import org.birdview.source.jira.model.JiraUser
import org.birdview.utils.BVFilters
import javax.inject.Named

@Named
open class JiraTaskService(
        private val jiraClientProvider: JiraClientProvider,
        sourcesConfigProvider: BVSourcesConfigProvider,
        private val jqlBuilder: JqlBuilder,
        private val bvUsersConfigProvider: BVUsersConfigProvider
): BVTaskSource {
    companion object {
        const val JIRA_KEY_TYPE = "jiraKey"
    }
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private val jiraConfigs: List<BVJiraConfig> = sourcesConfigProvider.getConfigsOfType(BVJiraConfig::class.java)

    override fun getTasks(user: String?, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit) {
        jiraConfigs.forEach { config -> getTasks(user, updatedPeriod, config, chunkConsumer) }
    }

    private fun getTasks(
            user: String?,
            updatedPeriod: TimeIntervalFilter,
            config: BVJiraConfig,
            chunkConsumer: (List<BVDocument>) -> Unit) {
        jiraClientProvider
                .getJiraClient(config)
                .findIssues(jqlBuilder.getJql(user, updatedPeriod, config)) { jiraIssues->
                    chunkConsumer.invoke(
                            jiraIssues.map { mapDocument( it, config) }
                    )
                }
    }

    override fun canHandleId(id: String): Boolean = BVFilters.JIRA_KEY_REGEX.matches(id)

    override fun loadByIds(keyList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit): Unit {
        jiraConfigs.forEach { config ->
            var client = jiraClientProvider.getJiraClient(config)
            client.findIssues(
                    "key IN (${keyList.distinct().joinToString(",")})") { issues->
                chunkConsumer.invoke(issues.map { mapDocument(it, config) })
            }
        }
    }

    private fun mapDocument(issue: JiraIssue, config: BVJiraConfig): BVDocument {
        val description = issue.fields.description ?: ""
        try {
            return BVDocument(
                    ids = setOf(BVDocumentId(id = issue.key, type = JIRA_KEY_TYPE, sourceName = config.sourceName)),
                    title = issue.fields.summary,
                    updated = dateTimeFormat.parse(issue.fields.updated),
                    created = dateTimeFormat.parse(issue.fields.created),
                    httpUrl = "${config.baseUrl}/browse/${issue.key}",
                    body = description,
                    refsIds = BVFilters.filterIdsFromText("${description} ${issue.fields.summary}"),
                    groupIds = extractGroupIds(issue, config.sourceName),
                    status = JiraIssueStatusMapper.toBVStatus(issue.fields.status.name),
                    operations = extractOperations(issue, config),
                    key = issue.key,
                    users = extractUsers(issue, config)
            )
        } catch (e:Exception) {
            throw RuntimeException("Could not parse issue $issue", e)
        }
    }

    private fun extractUsers(issue: JiraIssue, config: BVJiraConfig): List<BVDocumentUser> =
        listOfNotNull(
                mapDocumentUser(issue.fields.assignee, config.sourceName, UserRole.IMPLEMENTOR),
                mapDocumentUser(issue.fields.creator, config.sourceName, UserRole.CREATOR)
        )

    private fun mapDocumentUser(jiraUser: JiraUser?, sourceName: String, userRole: UserRole): BVDocumentUser? =
        bvUsersConfigProvider.getUserAlias(jiraUser?.emailAddress, sourceName)
                ?.let { alias -> BVDocumentUser(alias, userRole) }

    private fun extractOperations(issue: JiraIssue, config: BVJiraConfig): List<BVDocumentOperation> =
        issue.changelog
                ?.histories
                ?.flatMap (this::toOperation)
                ?.filter { it.author.equals(config.user) }
                ?: emptyList()

    private fun toOperation(changelogItem: JiraChangelogItem): List<BVDocumentOperation> =
            changelogItem.items.map { historyItem ->
                BVDocumentOperation(
                        author = changelogItem.author.emailAddress ?: "???",
                        description = historyItem.field,
                        created = dateTimeFormat.parse(changelogItem.created)
                )
            }

    override fun getType() = "jira"

    private fun extractGroupIds(issue: JiraIssue, sourceName: String): Set<BVDocumentId> =
            (issue.fields.customfield_10007?.let { setOf(BVDocumentId(it, JIRA_KEY_TYPE, sourceName)) } ?: emptySet<BVDocumentId>()) +
                    (issue.fields.parent?.let{ setOf(BVDocumentId(it.key, JIRA_KEY_TYPE, sourceName)) } ?: emptySet<BVDocumentId>())
}