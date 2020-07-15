package org.birdview.source.jira

import org.birdview.config.BVJiraConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.source.BVTaskListsDefaults
import org.birdview.source.jira.model.JiraIssue
import org.birdview.source.jira.model.JiraIssuesFilterRequest
import org.birdview.source.jira.model.JiraIssuesFilterResponse
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.WebTargetFactory
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget

class JiraClient(
        private val jiraConfig: BVJiraConfig,
        private val taskListDefaults: BVTaskListsDefaults,
        private val usersConfigProvider: BVUsersConfigProvider) {
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    fun findIssues(filter: JiraIssuesFilter): List<JiraIssue> =
            findIssues(getJql(filter))

    private fun getJql(filter: JiraIssuesFilter): String =
            "(assignee = ${getUser(filter.userAlias)}" +
                " or creator = ${getUser(filter.userAlias)}" +
                    ")" +
                (filter.issueStatuses?.let { " and status in (${it.joinToString(",") { "\"${it}\"" }})" } ?: "") +
                (filter.since?.let {  " and updatedDate >= \"${it.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))}\" " } ?: "") +
                " order by lastViewed DESC"

    private fun getUser(userAlias: String?): String =
            if(userAlias == null) { "currentUser()" }
            else { "\"${usersConfigProvider.getUserName(userAlias, jiraConfig.sourceName)}\""}

    fun findIssues(jql: String): List<JiraIssue> {
        val jiraRestTarget = getTarget()

        val jiraIssuesResponse = jiraRestTarget.path("search")
                .request()
                .post(Entity.json(JiraIssuesFilterRequest(
                maxResults = taskListDefaults.getMaxResult(),
                fields = arrayOf("*all"),
                jql = jql
        )))

        if(jiraIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Jira tasks: ${jiraIssuesResponse.readEntity(String::class.java)}")
        }

        val issues = jiraIssuesResponse.readEntity(JiraIssuesFilterResponse::class.java)
                .issues
                .map { executor.submit(Callable { loadIssue(it.self) }) }
                .mapNotNull { future -> try {
                    future.get()
                } catch (e:Exception) {
                    e.printStackTrace()
                    null
                }}

//        val maybeIssue = issues.firstOrNull()?.self?.let (this::loadIssue)
//        println(maybeIssue)
        // println(jiraIssuesResponse.readEntity(String::class.java))

        return issues
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


    fun loadIssues(keys:List<String>): List<JiraIssue> {
        val issues = findIssues("issueKey IN (${keys.joinToString(",")})");
        return issues;
    }

    private fun getTarget(): WebTarget = getTargetFactory().getTarget("/rest/api/2")

    private fun getTargetFactory() = getTargetFactory(jiraConfig.baseUrl)

    private fun getTargetFactory(url: String) = WebTargetFactory(url) {
        BasicAuth(jiraConfig.user, jiraConfig.token)
    }
}