package org.birdview.source.github

import org.birdview.source.ItemsPage
import org.birdview.source.github.model.*
import org.birdview.storage.BVGithubConfig
import org.birdview.utils.BVTimeUtil
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

class GithubClient (
        private val githubConfig: BVGithubConfig
) {
    private val log = LoggerFactory.getLogger(GithubClient::class.java)
    private val issuesPerPage = 100

    fun getIssueComments(pullRequest: GithubPullRequest) =
            BVTimeUtil.logTime("getIssueComments") {
                getTarget(pullRequest.comments_url)
                        ?.request()
                        ?.get()
                        ?.also(ResponseValidationUtils::validate)
                        ?.readEntity(Array<GithubIssueComment>::class.java)
                        ?.asList()
                        ?: emptyList()
            }

    fun getReviewComments(pullRequest: GithubPullRequest) =
            BVTimeUtil.logTime("getReviewComments") {
                getTarget(pullRequest.review_comments_url)
                        ?.request()
                        ?.get()
                        ?.also(ResponseValidationUtils::validate)
                        ?.readEntity(Array<GithubReviewComment>::class.java)
                        ?.asList()
                        ?: emptyList()
            }

    fun getIssueEvents(issue: GithubIssue) =
            BVTimeUtil.logTime("getIssueEvents") {
                getTarget(issue.events_url)
                        ?.request()
                        ?.get()
                        ?.also(ResponseValidationUtils::validate)
                        ?.readEntity(Array<GithubIssueEvent>::class.java)
                        ?.asList()
                        ?: emptyList()
            }

    fun findIssues(query:String, chunkConsumer: (List<GithubIssue>) -> Unit) {
        BVTimeUtil.logTime("findIssues") {
            log.info("Running github query '{}'", query)

            var target: WebTarget? = getTarget()
                    ?.path("search")
                    ?.path("issues")
                    ?.queryParam("q", query)
                    ?.queryParam("per_page", issuesPerPage)
            do {
                log.info("Loading github issues next page: {}", target?.uri)
                var page = target?.request()?.get()
                        ?.let(this::mapIssuesPage)
                        ?.takeUnless { it.items.isEmpty() }
                        ?.also { page ->
                            chunkConsumer.invoke(page.items)
                        }

                target = page?.let(ItemsPage<GithubIssue, String>::continuation)
                        ?.let(this::getTarget)
            } while (target != null)
        }
    }

    fun getPullRequest(prUrl: String): GithubPullRequest? =
            BVTimeUtil.logTime("getPullRequest") {
                getTarget(prUrl)
                        ?.request()
                        ?.get()
                        ?.also(ResponseValidationUtils::validate)
                        ?.readEntity(GithubPullRequest::class.java)
            }

    fun getPrCommits(pr: GithubPullRequest): List<GithubPrCommitContainer> =
            BVTimeUtil.logTime("getPrCommits") {
                getTarget(pr.commits_url)
                        ?.request()
                        ?.get()
                        ?.also(ResponseValidationUtils::validate)
                        ?.readEntity(Array<GithubPrCommitContainer>::class.java)
                        ?.asList()
                        ?: emptyList()
            }


    private fun mapIssuesPage(response: Response): ItemsPage<GithubIssue, String> =
            response
                    .also (ResponseValidationUtils::validate)
                    .let { resp ->
                        var githubIssues = resp.readEntity(GithubSearchIssuesResponse::class.java).items.asList()
                        //log.info("retrieved github issues:{}", githubIssues.map { "${it.pull_request?.html_url}" })
                        ItemsPage (
                                githubIssues,
                                resp.links.firstOrNull { "next" == it.rel } ?.uri ?.toString())
                    }

    private fun getConfig(): BVGithubConfig?  = githubConfig

    private fun getTarget() = getConfig()
        ?.let { config-> getTarget(config.baseUrl) }

    private fun getTarget(url:String) = getConfig()
        ?.let { config-> WebTargetFactory(url) {
            BasicAuth(config.user, config.token)
        }.getTarget("") }


}