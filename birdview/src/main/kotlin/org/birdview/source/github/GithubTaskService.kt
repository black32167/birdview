package org.birdview.source.github

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentOperation
import org.birdview.config.BVGithubConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.model.ReportType
import org.birdview.request.TasksRequest
import org.birdview.source.BVTaskSource
import org.birdview.source.github.model.*
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVFilters
import java.lang.Exception
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
class GithubTaskService(
        val sourcesConfigProvider: BVSourcesConfigProvider,
        val githubClientProvider: GithubClientProvider
): BVTaskSource {
    companion object {
        val GITHUB_ID = "githubId"
    }
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    // TODO: parallelize
    override fun getTasks(request: TasksRequest): List<BVDocument> =
            sourcesConfigProvider.getConfigsOfType(BVGithubConfig::class.java)
                    .flatMap { config-> getTasks(request, config) }

    private fun getTasks(request: TasksRequest, githubConfig:BVGithubConfig): List<BVDocument> {
        val client = githubClientProvider.getGithubClient(githubConfig)
        val state = getIssueStates(request.reportType)
        return client.getPullRequestIssues(state, request.since, request.user)
                .map { issue: GithubIssue -> executor.submit(Callable {
                    getPr(issue, client)
                            ?.let { pr -> toBVDocument(pr, issue, client, githubConfig) }
                })}
                .mapNotNull (Future<BVDocument?>::get)
    }

    private fun toBVDocument(pr: GithubPullRequest, issue: GithubIssue, client: GithubClient, githubConfig:BVGithubConfig): BVDocument {
        val description = pr.body ?: ""
        return BVDocument(
                ids = setOf(BVDocumentId(id = pr.id, type = GITHUB_ID, sourceName = githubConfig.sourceName)),
                title = pr.title,
                body = description,
                updated = parseDate(pr.updated_at),
                created = parseDate(pr.created_at),
                httpUrl = pr.html_url,
                refsIds = BVFilters.filterIdsFromText("${description} ${pr.title}") +
                        BVFilters.filterIdsFromText(pr.head.ref),
                groupIds = setOf(),
                status = pr.state,
                operations = extractOperations(pr, issue, client, githubConfig)
        )
    }

    private fun extractOperations(pr: GithubPullRequest, issue: GithubIssue, client: GithubClient, githubConfig:BVGithubConfig): List<BVDocumentOperation> {
        val issueEventsFuture = executor.submit(Callable { client.getIssueEvents(issue) })
        val issueCommentsFuture = executor.submit(Callable { client.getIssueComments(pr)} )
        val reviewComments = client.getReviewComments(pr)
        return (reviewComments.map { toOperation(it) } +
                issueEventsFuture.get().map { toOperation(it) } +
                issueCommentsFuture.get().map { toOperation(it) })
              //!!!  .filter { it.author ==  githubConfig.user }
                .sortedByDescending { it.created }
    }

    private fun toOperation(event: GithubIssueEvent) =
            BVDocumentOperation(
                    description = event.event,
                    author = event.actor.login,
                    created = parseDate(event.created_at)
            )

    private fun toOperation(comment: GithubReviewComment) =
            BVDocumentOperation(
                    description = "reviewed",
                    author = comment.user.login,
                    created = parseDate(comment.created_at)
            )

    private fun toOperation(comment: GithubIssueComment) =
            BVDocumentOperation(
                    description = "commented",
                    author = comment.user.login,
                    created = parseDate(comment.created_at)
            )

    private fun parseDate(date:String):Date = try {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date)
    } catch (e: Exception) {
        e.printStackTrace()
        Date()
    }
    override fun getType() = "github"

    private fun getPr(issue: GithubIssue, githubClient:GithubClient): GithubPullRequest? =
            issue.pull_request?.url
                    ?.let { url -> githubClient.getPullRequest(url) }

    private fun getIssueStates(reportType: ReportType):String? =
        when(reportType) {
            ReportType.DONE ->  null
            ReportType.PLANNED  ->  "open"
            else -> "none"
        }
}