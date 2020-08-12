package org.birdview.source.github

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentUser
import org.birdview.config.BVGithubConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.github.model.*
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
open class GithubTaskService(
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        private val githubClientProvider: GithubClientProvider,
        private val githubQueryBuilder: GithubQueryBuilder,
        private val bvUsersConfigProvider: BVUsersConfigProvider
): BVTaskSource {
    companion object {
        const val GITHUB_ID = "githubId"
    }
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    override fun getTasks(user: String?, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit) {
        sourcesConfigProvider.getConfigsOfType(BVGithubConfig::class.java)
                .forEach { config -> getTasks(user, updatedPeriod, config, chunkConsumer) }
    }

    private fun getTasks(
            user: String?,
            updatedPeriod: TimeIntervalFilter,
            githubConfig:BVGithubConfig,
            chunkConsumer: (List<BVDocument>) -> Unit) {
        val client = githubClientProvider.getGithubClient(githubConfig)
        val githubQuery = githubQueryBuilder.getFilterQueries(user, updatedPeriod, githubConfig)
        client.findIssues(githubQuery) { issues->
            val docs = issues.map { issue: GithubIssue ->
                executor.submit(Callable {
                    getPr(issue, client)
                        ?.let { pr -> toBVDocument(pr, issue, client, githubConfig) }
                })
            }.mapNotNull(Future<BVDocument?>::get)
            chunkConsumer.invoke(docs)
        }
    }

    private fun toBVDocument(pr: GithubPullRequest, issue: GithubIssue, client: GithubClient, githubConfig:BVGithubConfig): BVDocument {
        val description = pr.body ?: ""
        val title = BVFilters.removeIdsFromText(pr.title)
        val operations = extractOperations(pr, issue, client, sourceName = githubConfig.sourceName)
        val status = mapStatus(pr.state)
        return BVDocument(
                ids = setOf(BVDocumentId(id = pr.id, type = GITHUB_ID, sourceName = githubConfig.sourceName)),
                title = title,
                body = description,
                updated = parseDate(pr.updated_at),
                created = parseDate(pr.created_at),
                closed = extractClosed(operations, status),
                httpUrl = pr.html_url,
                refsIds = BVFilters.filterIdsFromText("${description} ${pr.title}") +
                        BVFilters.filterIdsFromText(pr.head.ref),
                groupIds = setOf(),
                status = status,
                operations = operations,
                key = pr.html_url.replace(".*/".toRegex(), "#"),
                users = extractUsers(pr, githubConfig)
        )
    }

    private fun extractClosed(operations: List<BVDocumentOperation>, status: BVDocumentStatus?): Date? =
            if (status == BVDocumentStatus.DONE) {
                operations.find { operation -> operation.description == "merged" } ?.created
            } else {
                null
            }

    private fun extractUsers(pr: GithubPullRequest, config: BVGithubConfig): List<BVDocumentUser> =
            listOf(UserRole.CREATOR, UserRole.IMPLEMENTOR).mapNotNull { mapDocumentUser(pr.user, config.sourceName, it) } +
                    listOfNotNull(mapDocumentUser(pr.assignee, config.sourceName, UserRole.IMPLEMENTOR)) +
                    pr.requested_reviewers.mapNotNull { reviewer -> mapDocumentUser(reviewer, config.sourceName, UserRole.WATCHER) }

    private fun mapDocumentUser(githubUser: GithubUser?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            bvUsersConfigProvider.getUser(githubUser?.login, sourceName, userRole)

    private fun mapStatus(state: String): BVDocumentStatus? = when (state) {
        "open" -> BVDocumentStatus.PROGRESS
        "closed" -> BVDocumentStatus.DONE
        else -> null
    }

    private fun extractOperations(pr: GithubPullRequest, issue: GithubIssue, client: GithubClient, sourceName: String): List<BVDocumentOperation> {
        val issueEventsFuture = executor.submit(Callable { client.getIssueEvents(issue) })
        val issueCommentsFuture = executor.submit(Callable { client.getIssueComments(pr)} )
        val reviewComments = client.getReviewComments(pr)
        return (reviewComments.map { toOperation(it, sourceName) } +
                issueEventsFuture.get().map { toOperation(it, sourceName) } +
                issueCommentsFuture.get().map { toOperation(it, sourceName) })
                .sortedByDescending { it.created }
    }

    private fun toOperation(event: GithubIssueEvent, sourceName: String) =
            BVDocumentOperation(
                    description = event.event,
                    author = event.actor.login,
                    created = parseDate(event.created_at),
                    sourceName = sourceName
            )

    private fun toOperation(comment: GithubReviewComment, sourceName: String) =
            BVDocumentOperation(
                    description = "reviewed",
                    author = comment.user.login,
                    created = parseDate(comment.created_at),
                    sourceName = sourceName
            )

    private fun toOperation(comment: GithubIssueComment, sourceName: String) =
            BVDocumentOperation(
                    description = "commented",
                    author = comment.user.login,
                    created = parseDate(comment.created_at),
                    sourceName = sourceName
            )

    private fun parseDate(date:String) =
            BVDateTimeUtils.parse(date, "yyyy-MM-dd'T'HH:mm:ss'Z'")

    override fun getType() = SourceType.GITHUB

    override fun isAuthenticated(sourceName: String): Boolean =
            sourcesConfigProvider.getConfigByName(sourceName, BVGithubConfig::class.java) != null

    private fun getPr(issue: GithubIssue, githubClient:GithubClient): GithubPullRequest? = try {
        issue.pull_request?.url
                ?.let { url -> githubClient.getPullRequest(url) }
    } catch (e:Exception) {
        e.printStackTrace()
        null
    }
}