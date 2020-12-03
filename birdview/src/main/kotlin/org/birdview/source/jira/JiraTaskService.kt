package org.birdview.source.jira

import org.birdview.analysis.*
import org.birdview.model.*
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.jira.model.*
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
    private val JIRA_REST_URL_REGEX = "https?://.*/rest/api/2/issue/.*".toRegex()
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

    override fun canHandleId(id: String): Boolean =
            BVFilters.JIRA_KEY_REGEX.matches(id) || JIRA_REST_URL_REGEX.matches(id)

    override fun loadByIds(sourceName: String, idList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
        val issueKeys = idList.filter { BVFilters.JIRA_KEY_REGEX.matches(it) }
        val issueUrls = idList.filter { JIRA_REST_URL_REGEX.matches(it) }
        sourceSecretsStorage.getConfigByName(sourceName, BVJiraConfig::class.java)?.also { config ->
            val client = jiraClientProvider.getJiraClient(config)
            client.loadByKeys(issueKeys.distinct()) { issues ->
                chunkConsumer.invoke(issues.map { mapDocument(it, config) })
            }
            val docsByUrls = issueUrls.distinct()
                    .map { url->executor.submit(Callable { client.loadByUrl(url) }) }
                    .map {
                        mapDocument(it.get(), config)
                    }
            chunkConsumer.invoke(docsByUrls)
        }
    }

    private fun mapDocument(issue: JiraIssue, config: BVJiraConfig): BVDocument {
        val description = issue.fields.description ?: ""
        val issueLinks = jiraClientProvider.getJiraClient(config).getIssueLinks(issue.key)

        try {
            return BVDocument(
                    ids = docId(issue.key, issue.self),
                    title = issue.fields.summary,
                    key = issue.key,
                    body = description,
                    updated = parseDate(issue.fields.updated),
                    created = parseDate(issue.fields.created),
                    httpUrl = "${config.baseUrl}/browse/${issue.key}",
                    users = extractUsers(issue, config),
                    refs = extractRefsIds(issue, issueLinks),
                    status = JiraIssueStatusMapper.toBVStatus(issue.fields.status.name),
                    operations = extractOperations(issue, config),
                    sourceType = getType(),
                    priority = extractPriority(issue),
                    sourceName = config.sourceName
            )
        } catch (e:Exception) {
            throw RuntimeException("Could not parse issue $issue", e)
        }
    }

    private fun docId(vararg ids:String) = ids.map { BVDocumentId(it, sourceType = SourceType.JIRA) }.toSet()

    private fun extractRefsIds(issue: JiraIssue, issueLinks: Array<JiraRemoteLink>): List<BVDocumentRef> {
        val textIds = BVFilters.filterRefsFromText("${issue.fields.description ?: ""} ${issue.fields.summary}")
                .map(::BVDocumentRef)
        val issueExternalLinks = issueLinks.map { it._object.url }
                .flatMap { BVFilters.filterRefsFromText(it) }
                .map(::BVDocumentRef)
        val issueLinkIds = issue.fields.issuelinks?.mapNotNull { jiraIssueLink->
            mapIssueLink(jiraIssueLink)
        } ?: listOf()
        val parentIds = listOfNotNull(issue.fields.customfield_10007, issue.fields.parent?.key)
                .map { BVDocumentRef(BVDocumentId(it, SourceType.JIRA), RelativeHierarchyPosition.LINK_TO_PARENT)  }
        return issueExternalLinks + issueLinkIds + textIds + parentIds
    }

    private fun mapIssueLink(jiraIssueLink: JiraIssueLink): BVDocumentRef? {
        val referencedIssue = jiraIssueLink.run { outwardIssue ?: inwardIssue }
                ?: return null

        val issueUrl = referencedIssue.self

        val outwardToken = jiraIssueLink.type.outward.toLowerCase()
        val refInfo = BVDocumentId(issueUrl)
        return when (outwardToken) {
            "blocks", "contributes to", "split to", "resolves", "has to be done before", "has to be finished together with" ->
                BVDocumentRef(refInfo, RelativeHierarchyPosition.LINK_TO_PARENT)
            "depends on" ->
                BVDocumentRef(refInfo, RelativeHierarchyPosition.LINK_TO_CHILD)
            "relates on", "relates to" ->
                BVDocumentRef(refInfo)
            "duplicates", "clones"->
                BVDocumentRef(refInfo)
            else -> BVDocumentRef(refInfo)
        }
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
                ?.filter { it.author == config.user }
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

}