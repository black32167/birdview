package org.birdview.source.jira

import org.birdview.analysis.*
import org.birdview.model.BVDocumentRef
import org.birdview.model.RelativeHierarchyType
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.jira.model.*
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Named

@Named
open class JiraTaskService(
    private val jiraClient: JiraClient,
    private val jqlBuilder: JqlBuilder,
    private val userSourceConfigProvider: BVSourceConfigProvider,

    ): BVTaskSource {
    private val JIRA_REST_URL_REGEX = "https?://.*/rest/api/2/issue/.*".toRegex()
    private val JIRA_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory("JiraTaskService"))

    override fun getTasks(
        bvUser: String,
        updatedPeriod: TimeIntervalFilter,
        sourceConfig: BVSourceConfigProvider.SyntheticSourceConfig,
        chunkConsumer: BVSessionDocumentConsumer
    ) {
        val sourceUserName = sourceConfig.sourceUserName
        jiraClient
                .findIssues(bvUser = bvUser, sourceName = sourceConfig.sourceName, jqlBuilder.getJql(sourceUserName, updatedPeriod)) { jiraIssues->
                    chunkConsumer.consume(
                            jiraIssues
                                    .map { executor.submit(Callable<BVDocument> {
                                        mapDocument( it, bvUser = bvUser, sourceName = sourceConfig.sourceName, baseUrl = sourceConfig.baseUrl) }) }
                                    .map { it.get() }
                    )
                }
    }

    override fun canHandleId(id: String): Boolean =
            BVFilters.JIRA_KEY_REGEX.matches(id) || JIRA_REST_URL_REGEX.matches(id)

    override fun loadByIds(bvUser: String, sourceName: String, keyList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
        val issueKeys = keyList.filter { BVFilters.JIRA_KEY_REGEX.matches(it) }
        val issueUrls = keyList.filter { JIRA_REST_URL_REGEX.matches(it) }
        userSourceConfigProvider.getSourceConfig(
            sourceName = sourceName, bvUser = bvUser)?.also { config ->
            val client = jiraClient
            client.loadByKeys(bvUser = bvUser, sourceName = sourceName, issueKeys.distinct()) { issues ->
                chunkConsumer.invoke(issues.map { mapDocument(it, bvUser = bvUser, sourceName = sourceName, baseUrl = config.baseUrl) })
            }
            val docsByUrls = issueUrls.distinct()
                    .map { url->executor.submit(Callable { client.loadByUrl(bvUser = bvUser, sourceName = sourceName, url = url) }) }
                    .map {
                        mapDocument(it.get(), bvUser = bvUser, sourceName = sourceName, baseUrl = config.baseUrl)
                    }
            chunkConsumer.invoke(docsByUrls)
        }
    }

    private fun mapDocument(issue: JiraIssue, bvUser: String, sourceName: String, baseUrl: String): BVDocument {
        val description = issue.fields.description ?: ""
        val issueLinks = jiraClient.getIssueLinks(bvUser = bvUser, sourceName = sourceName, issue.key)

        try {
            return BVDocument(
                    ids = docId(issue.key, issue.self),
                    title = issue.fields.summary,
                    key = issue.key,
                    body = description,
                    updated = parseDate(issue.fields.updated),
                    created = parseDate(issue.fields.created),
                    httpUrl = "${baseUrl}/browse/${issue.key}",
                    users = extractUsers(issue, sourceName = sourceName),
                    refs = extractRefsIds(issue, issueLinks),
                    status = JiraIssueStatusMapper.toBVStatus(issue.fields.status.name),
                    operations = extractOperations(bvUser = bvUser, issue, sourceName = sourceName),
                    priority = extractPriority(issue),
                    sourceName = sourceName
            )
        } catch (e:Exception) {
            throw RuntimeException("Could not parse issue $issue", e)
        }
    }

    private fun docId(vararg ids:String) = ids.map { BVDocumentId(it, sourceType = SourceType.JIRA) }.toSet()

    private fun extractRefsIds(issue: JiraIssue, issueLinks: Array<JiraRemoteLink>): List<BVDocumentRef> {
        val textIds = BVFilters.filterRefsFromText("${issue.fields.description ?: ""} ${issue.fields.summary}")
                .map(::BVDocumentRef)
        val issueExternalLinks = mapExternalLinks(issueLinks)
        val issueLinkIds = issue.fields.issuelinks?.mapNotNull { jiraIssueLink->
            mapIssueLink(jiraIssueLink)
        } ?: listOf()
        val parentIds = listOfNotNull(issue.fields.customfield_10007, issue.fields.parent?.key)
                .map { BVDocumentRef(BVDocumentId(it, SourceType.JIRA), RelativeHierarchyType.LINK_TO_PARENT)  }
        return issueExternalLinks + issueLinkIds + textIds + parentIds
    }

    private fun mapExternalLinks(remoteLinks: Array<JiraRemoteLink>) =
        remoteLinks
          //  .filter { it.relationship?.contains("mentioned")?.not() ?: true }
            .map { it._object.url }
            .flatMap { BVFilters.filterRefsFromText(it) }
            .map { id->BVDocumentRef(id, RelativeHierarchyType.LINK_TO_CHILD) }

    private fun mapIssueLink(jiraIssueLink: JiraIssueLink): BVDocumentRef? {
        val referencedIssue = jiraIssueLink.run { outwardIssue ?: inwardIssue }
                ?: return null

        val issueUrl = referencedIssue.self

        val outwardToken = jiraIssueLink.type.outward.toLowerCase()
        val refInfo = BVDocumentId(issueUrl)
        return when (outwardToken) {
            "blocks", "contributes to", "split to", "resolves", "has to be done before", "has to be finished together with" ->
                BVDocumentRef(refInfo, RelativeHierarchyType.LINK_TO_DEPENDENT)
            "depends on" ->
                BVDocumentRef(refInfo, RelativeHierarchyType.LINK_TO_BLOCKER)
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

    private fun extractUsers(issue: JiraIssue, sourceName: String): List<BVDocumentUser> =
        listOfNotNull(
                mapDocumentUser(issue.fields.assignee, sourceName, UserRole.IMPLEMENTOR)
        )

    private fun mapDocumentUser(jiraUser: JiraUser?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            jiraUser?.emailAddress
                    ?.let { emailAddress -> BVDocumentUser(emailAddress, userRole, sourceName) }

    private fun extractOperations(bvUser: String, issue: JiraIssue, sourceName: String): List<BVDocumentOperation> {
        val issueStatesOperations = issue.changelog
            ?.histories
            ?.flatMap { toOperation(issue, it, sourceName) }
            ?: emptyList()
        val issueCommentsOperations = jiraClient.getIssueComments(bvUser = bvUser, sourceName = sourceName, issueKey = issue.key)
            .comments
            .map { toOperation (issue, it, sourceName) }
        return (issueCommentsOperations + issueStatesOperations).sortedByDescending { it.created }
    }

    private fun toOperation(issue: JiraIssue, comment: JiraComment, sourceName: String): BVDocumentOperation =
        BVDocumentOperation(
            author = comment.updateAuthor.emailAddress ?: "???",
            description = "comment",
            created = parseDate(comment.updated),
            sourceName = sourceName,
            type = if (comment.updateAuthor.emailAddress == issue.fields.assignee?.emailAddress) {
                BVDocumentOperationType.UPDATE
            } else {
                BVDocumentOperationType.COMMENT
            }
        )

    private fun toOperation(issue: JiraIssue, changelogItem: JiraChangelogItem, sourceName: String): List<BVDocumentOperation> =
            changelogItem.items.map { historyItem ->
                BVDocumentOperation(
                    author = changelogItem.author.emailAddress ?: "???",
                    description = historyItem.field,
                    created = parseDate(changelogItem.created),
                    sourceName = sourceName,
                    type = if (changelogItem.author.emailAddress == issue.fields.assignee?.emailAddress) {
                        BVDocumentOperationType.UPDATE
                    } else {
                        BVDocumentOperationType.COMMENT
                    }
                )
            }

    override fun getType() = SourceType.JIRA
}