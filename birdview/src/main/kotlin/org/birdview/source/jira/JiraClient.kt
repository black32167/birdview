package org.birdview.source.jira

import org.birdview.config.BVJiraConfig
import org.birdview.source.BVTaskListsDefaults
import org.birdview.source.ItemsIterable
import org.birdview.source.ItemsPage
import org.birdview.source.jira.model.JiraIssue
import org.birdview.source.jira.model.JiraIssuesFilterRequest
import org.birdview.source.jira.model.JiraIssuesFilterResponse
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

class JiraClient(
        private val jiraConfig: BVJiraConfig,
        private val taskListDefaults: BVTaskListsDefaults) {
    private val log = LoggerFactory.getLogger(JiraClient::class.java)
    private val issuesPerPage = 2
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    fun findIssues(jql: String?): Iterable<JiraIssue> {
        if (jql == null) {
            return emptyList()
        }
        log.info("Running jql '{}'", jql)

        val jiraIssuesRequest = JiraIssuesFilterRequest(
                startAt = 0,
                maxResults = issuesPerPage,
                fields = arrayOf("*all"),
                jql = jql)
        return postIssuesSearch(jiraIssuesRequest)
                .let { resp->
                    ItemsIterable(mapIssuesPage (resp)) { startAt ->
                        log.info("Loading jira issues next page starting at: {}", startAt)
                        postIssuesSearch(jiraIssuesRequest.copy(startAt = startAt))
                                ?.let (this::mapIssuesPage)
                    }
                }
    }

    private fun postIssuesSearch(jiraIssuesRequest: JiraIssuesFilterRequest) =
            getTarget()
                    .path("search")
                    .request()
                    .post(Entity.json(jiraIssuesRequest))

    private fun mapIssuesPage(response: Response): ItemsPage<JiraIssue, Int> =
            response
                    .also (ResponseValidationUtils::validate)
                    .let { resp ->
                        val issuesResponse = resp.readEntity(JiraIssuesFilterResponse::class.java)
                        val issues = issuesResponse.issues
                                .map { executor.submit(Callable { loadIssue(it.self) }) }
                                .mapNotNull { future ->
                                    try {
                                        future.get()
                                    } catch (e:Exception) {
                                        log.error("", e)
                                        null
                                    }
                                }
                        ItemsPage (
                                issues,
                                issuesResponse.run { startAt + maxResults })
                    }

    private fun loadIssue(url:String): JiraIssue =
            getTargetFactory(url).getTarget("")
                .queryParam("expand", "changelog")
                .request()
                .get()
                .also { response -> if(response.status != 200) {
                    throw RuntimeException("Error reading Jira tasks: ${response.readEntity(String::class.java)}")
                } }
                .let { it.readEntity(JiraIssue::class.java) }

    fun loadIssues(keys:List<String>): Iterable<JiraIssue> =
            findIssues("issueKey IN (${keys.joinToString(",")})")

    private fun getTarget(): WebTarget = getTargetFactory().getTarget("/rest/api/2")

    private fun getTargetFactory() = getTargetFactory(jiraConfig.baseUrl)

    private fun getTargetFactory(url: String) = WebTargetFactory(url) {
        BasicAuth(jiraConfig.user, jiraConfig.token)
    }
}