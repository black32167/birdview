package org.birdview.source.jira

import org.birdview.config.BVJiraConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.source.BVTaskListsDefaults
import org.birdview.source.jira.model.JiraIssue
import org.birdview.source.jira.model.JiraIssuesFilterRequest
import org.birdview.source.jira.model.JiraIssuesFilterResponse
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.WebTargetFactory

import java.time.format.DateTimeFormatter
import javax.ws.rs.client.Entity

class JiraClient(
        private val jiraConfig: BVJiraConfig,
        private val taskListDefaults: BVTaskListsDefaults,
        val usersConfigProvider: BVUsersConfigProvider) {

    fun findIssues(filter: JiraIssuesFilter): Array<JiraIssue> =
            findIssues(getJql(filter))

    private fun getJql(filter: JiraIssuesFilter): String =
            "(assignee = ${getUser(filter.userAlias)}" +
                " or creator = ${getUser(filter.userAlias)}" +
                    ")" +
                (filter.issueStatus?.let { " and status in (\"${it}\")" } ?: "") +
                filter.since.let {  " and updatedDate >= \"${it.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))}\" " } +
                " order by lastViewed DESC"

    private fun getUser(userAlias: String?): String =
            if(userAlias == null) { "currentUser()" }
            else { "\"${usersConfigProvider.getUserName(userAlias, jiraConfig.sourceName)}\""}

    fun findIssues(jql: String): Array<JiraIssue> {
        val jiraRestTarget = WebTargetFactory(jiraConfig.baseUrl) {
            BasicAuth(jiraConfig.user, jiraConfig.token)
        }.getTarget("/rest/api/2")

        val jiraIssuesResponse = jiraRestTarget.path("search").request()
                .post(Entity.json(JiraIssuesFilterRequest(
                maxResults = taskListDefaults.getMaxResult(),
                fields = arrayOf("*all"),
                jql = jql
        )))

        if(jiraIssuesResponse.status != 200) {
            throw RuntimeException("Error reading Jira tasks: ${jiraIssuesResponse.readEntity(String::class.java)}")
        }

        // println(jiraIssuesResponse.readEntity(String::class.java))

        return jiraIssuesResponse.readEntity(JiraIssuesFilterResponse::class.java).issues
    }

    fun loadIssues(keys:List<String>): Array<JiraIssue> {
        val issues = findIssues("issueKey IN (${keys.joinToString(",")})");
        return issues;
    }
}