package org.birdview.source.github

import org.birdview.config.BVGithubConfig
import org.birdview.source.ItemsIterable
import org.birdview.source.ItemsPage
import org.birdview.source.github.model.*
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response

class GithubClient(
        private val githubConfig: BVGithubConfig
) {
    private val log = LoggerFactory.getLogger(GithubClient::class.java)
    private val issuesPerPage = 50

    fun getIssueComments(pullRequest: GithubPullRequest) =
        getTarget(pullRequest.comments_url)
                ?.request()
                ?.get()
                ?.also {
                    if(it.status != 200) {
                        throw java.lang.RuntimeException("Status:${it.status}, message=${it.readEntity(String::class.java)}")
                    }
                }
                ?.readEntity(Array<GithubIssueComment>::class.java)
                ?.asList()
                ?: emptyList()

    fun getReviewComments(pullRequest: GithubPullRequest) =
            getTarget(pullRequest.review_comments_url)
                    ?.request()
                    ?.get()
                    ?.also {
                        if(it.status != 200) {
                            throw java.lang.RuntimeException("Status:${it.status}, message=${it.readEntity(String::class.java)}")
                        }
                    }
                    ?.readEntity(Array<GithubReviewComment>::class.java)
                    ?.asList()
                    ?: emptyList()

    fun getIssueEvents(issue: GithubIssue) =
            getTarget(issue.events_url)
                    ?.request()
                    ?.get()
                    ?.also {
  //                      println(it.readEntity(String::class.java))
                        if(it.status != 200) {
                            throw java.lang.RuntimeException("Status:${it.status}, message=${it.readEntity(String::class.java)}")
                        }
                    }
                    ?.readEntity(Array<GithubIssueEvent>::class.java)
                    ?.asList()
                    ?: emptyList()

    fun findIssues(query:String): Iterable<GithubIssue> {
        log.info("Running github query '{}'", query)
        return getTarget()
                ?.path("search")
                ?.path("issues")
                ?.queryParam("q", query)
                ?.queryParam("per_page", issuesPerPage)
                ?.request()
                ?.get()
                ?.let { resp ->
                    ItemsIterable(mapIssuesPage (resp)) { url ->
                        log.info("Loading github issues next page: {}", url)
                        getTarget(url)?.request()?.get()?.let (this::mapIssuesPage)
                    }
                }
                ?: ItemsIterable<GithubIssue, String>()
    }

    private fun mapIssuesPage(response: Response): ItemsPage<GithubIssue, String> =
            response
                    .also (ResponseValidationUtils::validate)
                    .let { resp ->
                        ItemsPage (
                                resp.readEntity(GithubSearchIssuesResponse::class.java).items.asList(),
                                resp.links.firstOrNull { "next" == it.rel } ?.uri ?.toString())
                    }

    private fun getConfig(): BVGithubConfig?  = githubConfig

    fun getPullRequest(prUrl: String): GithubPullRequest? =
            getTarget(prUrl)
                    ?.request()
                    ?.get()
                    ?.readEntity(GithubPullRequest::class.java)

    private fun getTarget() = getConfig()
        ?.let { config-> getTarget(config.baseUrl) }

    private fun getTarget(url:String) = getConfig()
        ?.let { config-> WebTargetFactory(url) {
            BasicAuth(config.user, config.token)
        }.getTarget("") }
}