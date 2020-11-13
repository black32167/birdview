package org.birdview.source.jira

import org.birdview.analysis.*
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVRefDirection
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVDocIdTypes.JIRA_KEY_TYPE
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.jira.model.JiraChangelogItem
import org.birdview.source.jira.model.JiraIssue
import org.birdview.source.jira.model.JiraRemoteLink
import org.birdview.source.jira.model.JiraUser
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVJiraConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Named

@Named
open class JiraTaskService(
        private val jiraClientProvider: JiraClientProvider,
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val jqlBuilder: JqlBuilder
): BVTaskSource {
    private val JIRA_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory("JiraTaskService"))

    override fun getTasks(
            bvUser: String,
            updatedPeriod: TimeIntervalFilter,
            sourceConfig: BVAbstractSourceConfig,
            chunkConsumer: (List<BVDocument>) -> Unit) {
        val jiraConfig = sourceConfig as BVJiraConfig
        jiraClientProvider
                .getJiraClient(jiraConfig)
                .findIssues(jqlBuilder.getJql(bvUser, updatedPeriod, jiraConfig)) { jiraIssues->
                    chunkConsumer.invoke(
                            jiraIssues
                                    .map { executor.submit(Callable<BVDocument> { mapDocument( it, jiraConfig) }) }
                                    .map { it.get() }
                    )
                }
    }

    override fun canHandleId(id: String): Boolean = BVFilters.JIRA_KEY_REGEX.matches(id)

    override fun loadByIds(sourceName: String, keyList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit): Unit {
        sourceSecretsStorage.getConfigByName(sourceName, BVJiraConfig::class.java)?.also { config ->
            val client = jiraClientProvider.getJiraClient(config)
            client.loadByKeys(keyList.distinct()) {
                issues ->
                chunkConsumer.invoke(issues.map { mapDocument(it, config) })
            }
        }
    }

    private fun mapDocument(issue: JiraIssue, config: BVJiraConfig): BVDocument {
        val description = issue.fields.description ?: ""
        val issueLinks = jiraClientProvider.getJiraClient(config).getIssueLinks(issue.key)

        try {
            return BVDocument(
                    ids = setOf(BVDocumentId(id = issue.key, type = JIRA_KEY_TYPE, sourceName = config.sourceName)),
                    title = issue.fields.summary,
                    updated = parseDate(issue.fields.updated),
                    created = parseDate(issue.fields.created),
                    httpUrl = "${config.baseUrl}/browse/${issue.key}",
                    body = description,
                    refs = extractRefsIds(issue, issueLinks, config.sourceName),
                    groupIds = extractParentIds(issue, config.sourceName),
                    status = JiraIssueStatusMapper.toBVStatus(issue.fields.status.name),
                    operations = extractOperations(issue, config),
                    key = issue.key,
                    users = extractUsers(issue, config),
                    sourceType = getType(),
                    priority = extractPriority(issue)
            )
        } catch (e:Exception) {
            throw RuntimeException("Could not parse issue $issue", e)
        }
    }

    private fun extractRefsIds(issue: JiraIssue, issueLinks: Array<JiraRemoteLink>, sourceName: String): List<BVDocumentRef> {
        val ids = BVFilters.filterIdsFromText("${issue.fields.description ?: ""} ${issue.fields.summary}") +
                issueLinks.map { it._object.url } +
                extractParentIds(issue)
        issue.fields.issuelinks?.map {jiraIssueLink->
            val direction = when(jiraIssueLink.type.outward.toLowerCase()) {
                "blocks" -> BVRefDirection.CHILD
                else -> BVRefDirection.UNSPECIFIED
            }
        }
        return ids.map { BVDocumentRef(it, sourceName = sourceName) }
    }

    private fun extractPriority(issue: JiraIssue): Priority = issue.fields.priority?.id?.let { Integer.parseInt(it) }
            ?.let { id ->
                if (id < 3) {
                    Priority.HIGH
                } else if (id < 4) {
                    Priority.NORMAL
                } else {
                    Priority.LOW
                }
            } ?: Priority.LOW

    private fun parseDate(dateTimeString:String?) =
            BVDateTimeUtils.parse(dateTimeString, JIRA_DATETIME_PATTERN)

    private fun extractUsers(issue: JiraIssue, config: BVJiraConfig): List<BVDocumentUser> =
        listOfNotNull(
                mapDocumentUser(issue.fields.assignee, config.sourceName, UserRole.IMPLEMENTOR),
                mapDocumentUser(issue.fields.creator, config.sourceName, UserRole.CREATOR)
        )

    private fun mapDocumentUser(jiraUser: JiraUser?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            jiraUser?.emailAddress
                    ?.let { emailAddress -> BVDocumentUser(emailAddress, userRole, sourceName) }

    private fun extractOperations(issue: JiraIssue, config: BVJiraConfig): List<BVDocumentOperation> =
        issue.changelog
                ?.histories
                ?.flatMap { toOperation(it, config.sourceName) }
                ?.filter { it.author.equals(config.user) }
                ?: emptyList()

    private fun toOperation(changelogItem: JiraChangelogItem, sourceName: String): List<BVDocumentOperation> =
            changelogItem.items.map { historyItem ->
                BVDocumentOperation(
                        author = changelogItem.author.emailAddress ?: "???",
                        description = historyItem.field,
                        created = parseDate(changelogItem.created),
                        sourceName = sourceName
                )
            }

    override fun getType() = SourceType.JIRA

    override fun isAuthenticated(sourceName: String): Boolean =
            sourceSecretsStorage.getConfigByName(sourceName, BVJiraConfig::class.java) != null

    private fun extractParentIds(issue: JiraIssue, sourceName: String): Set<BVDocumentId> =
            extractParentIds(issue).map { BVDocumentId(it, JIRA_KEY_TYPE, sourceName) }.toSet()

    private fun extractParentIds(issue: JiraIssue):Set<String> =
            listOfNotNull(issue.fields.customfield_10007, issue.fields.parent?.key).toSet()
}