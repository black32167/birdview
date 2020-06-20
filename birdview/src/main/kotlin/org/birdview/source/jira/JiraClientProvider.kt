package org.birdview.source.jira

import org.birdview.config.BVJiraConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class JiraClientProvider(
        private val taskListDefaults: BVTaskListsDefaults,
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getJiraClient(jiraConfig:BVJiraConfig) =
            JiraClient(jiraConfig, taskListDefaults, usersConfigProvider)
}