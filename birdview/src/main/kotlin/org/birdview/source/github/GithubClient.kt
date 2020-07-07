package org.birdview.source.github

import org.birdview.config.BVGithubConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.source.BVTaskListsDefaults
import org.birdview.source.github.model.*
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.remote.WebTargetFactory
import org.birdview.utils.remote.BasicAuth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import java.util.concurrent.Executors

//class GithubIssuesFilter(
//        val issueState: String?,
//        val since: ZonedDateTime?,
//        val user:String?
//)
class GithubClient(
        val githubConfig: BVGithubConfig,
        val sourceConfig: BVTaskListsDefaults,
        val usersConfigProvider: BVUsersConfigProvider
) {
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    fun getCurrentUserIssues(issueState: String, since:ZonedDateTime):List<GithubIssue> =
        getTarget()
            ?.let { githubRestTarget->
                val githubIssuesResponse = githubRestTarget.path("issues")
                        .queryParam("filter", "created")
                        .queryParam("state", issueState)
                        .queryParam("per_page", sourceConfig.getMaxResult())
                        .queryParam("since", since.format(DateTimeFormatter.ISO_DATE_TIME))
                        .request()
                        .get()

                if(githubIssuesResponse.status != 200) {
                    throw RuntimeException("Error reading Github issues: ${githubIssuesResponse.readEntity(String::class.java)}")
                }

                return githubIssuesResponse.readEntity(Array<GithubIssue>::class.java).toList()
            }
            ?: listOf<GithubIssue>()


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

    fun findIssues(filter:GithubIssuesFilter):List<GithubIssue> =
        getTarget()
                ?.path("search")
                ?.path("issues")
                ?.queryParam("q", getFilterQuery(filter))
//                ?.also {
//                    println("Url:${it.uri}")
//                }
                ?.request()
                ?.get()
                ?.also {
                    if(it.status != 200) {
                        throw java.lang.RuntimeException("Status:${it.status}, message=${it.readEntity(String::class.java)}")
                    }
//                    println(it.readEntity(String::class.java))
//                    println()
                }
                ?.readEntity(GithubSearchIssuesResponse::class.java)?.items
                ?.asList()
                ?:listOf()

    private fun getFilterQuery(filter: GithubIssuesFilter): String =
            "type:pr" +
            (filter.prState?.let{ " state:${it}" } ?: "") +
            (filter.repository?.let { " repo:${it}" } ?: "") +
            filter.userAlias.let { " author:${getGithubUser(it)}" } +
            (filter.since?.let { " updated:>=${it.format(DateTimeFormatter.ISO_LOCAL_DATE)}" } ?: "")

    private fun getGithubUser(userAlias: String?): String? =
            if (userAlias == null) "@me"
            else usersConfigProvider.getUserName(userAlias, githubConfig.sourceName)

    private fun getUserIdByEMail(email: String): String =
            getTarget()
                    ?.path("search")
                    ?.path("users")
                    ?.queryParam("q", "${email} in:email")
                    ?.request()
                    ?.get()
                    ?.also {
                        println("Status:${it.status}")
                        println(it.readEntity(String::class.java))
                        println("")
                    }
                    ?.readEntity(String::class.java)
                    ?:""

    fun getCurrentUserPullRequests(issueState: String, since: ZonedDateTime):List<GithubPullRequest> =
            getCurrentUserIssues(issueState, since)
                    .mapNotNull { issue -> // Get Pull Requests
                        issue.pull_request?.url
                                ?.let { pr_url -> executor.submit(Callable<GithubPullRequest> { getPullRequest(pr_url) }) }
                    }
                    .map { it.get() }

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