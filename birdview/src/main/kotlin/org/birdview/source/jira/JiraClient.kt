package org.birdview.source.jira

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.source.jira.model.JiraIssue
import org.birdview.source.jira.model.JiraIssuesFilterRequest
import org.birdview.source.jira.model.JiraIssuesFilterResponse
import org.birdview.source.jira.model.JiraRemoteLink
import org.birdview.storage.model.secrets.BVJiraConfig
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVTimeUtil
import org.birdview.utils.remote.BasicAuth
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
class JiraClient(
    private val httpClientFactory: BVHttpClientFactory
) {
    private val API_SUFFIX  = "/rest/api/2"
    private val log = LoggerFactory.getLogger(JiraClient::class.java)
    private val issuesPerPage = 50
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    fun findIssues(jiraConfig: BVJiraConfig, jql: String?, chunkConsumer: (List<JiraIssue>) -> Unit) {
        if (jql == null) {
            return
        }
        log.info("Running JQL '{}'", jql)

        val jiraIssuesRequest = JiraIssuesFilterRequest(
                startAt = 0,
                maxResults = issuesPerPage,
                fields = listOf("*all"),
                jql = jql)

        var startAt:Int? = 0
        do {
            val response = BVTimeUtil.logTimeAndReturn("jira-findIssues-page") {
                getHttpClient(jiraConfig)
                    .post(
                        resultClass = JiraIssuesFilterResponse::class.java,
                        subPath = "search",
                        postEntity = jiraIssuesRequest.copy(startAt = startAt!!))
                    .also { issuesResponse ->
                        val issues = issuesResponse.issues
                            .map { executor.submit(Callable { loadByUrl(jiraConfig, it.self) }) }
                            .mapNotNull { future ->
                                try {
                                    future.get()
                                } catch (e:Exception) {
                                    log.error(e.message)
                                    null
                                }
                            }

                        log.info("Loaded {} jira issues", issues.size)
                        chunkConsumer.invoke(issues)
                    }
            }
            startAt = response.startAt + response.issues.size
        } while (!response.isLast && !response.issues.isEmpty())
    }

    fun loadByKeys(jiraConfig: BVJiraConfig, issueKeys: List<String>, chunkConsumer: (List<JiraIssue>) -> Unit) {
        val issueFutures = ConcurrentHashMap<String, Future<JiraIssue>>()
        issueKeys.forEach { issueKey ->
            issueFutures.computeIfAbsent(issueKey) {
                val url = "${getApiRootUrl(jiraConfig)}/issue/${issueKey}"
                executor.submit(Callable { loadByUrl(jiraConfig, url) })
            }
        }
        val issues = issueFutures.mapNotNull { (issueKey, issueFuture) ->
            try {
                issueFuture.get()
            } catch (e:Exception) {
                log.error("Error loading issue #${issueKey}: ${e.message}")
                null
            }
        }

        chunkConsumer(issues)
    }

    fun loadByUrl(jiraConfig: BVJiraConfig, url:String): JiraIssue {
        if (!url.startsWith(getApiRootUrl(jiraConfig))) {
            throw IllegalArgumentException("Can't load ${url} from ${jiraConfig.baseUrl}")
        }
        return getHttpClient(jiraConfig).get(
            resultClass = JiraIssue::class.java,
            subPath = url.substring(getApiRootUrl(jiraConfig).length),
            parameters = mapOf("expand" to "changelog")
        )
    }

    fun getIssueLinks(jiraConfig: BVJiraConfig, issueKey: String): Array<JiraRemoteLink> =
        getHttpClient(jiraConfig).get(
            resultClass = Array<JiraRemoteLink>::class.java,
            subPath = "issue/${issueKey}/remotelink",
            parameters = mapOf("expand" to "changelog")
        )

    private fun getHttpClient(jiraConfig: BVJiraConfig) =
        httpClientFactory.getHttpClient(getApiRootUrl(jiraConfig)) {
            BasicAuth(jiraConfig.user, jiraConfig.token)
        }

    private fun getApiRootUrl(jiraConfig: BVJiraConfig) = "${jiraConfig.baseUrl}${API_SUFFIX}"
}