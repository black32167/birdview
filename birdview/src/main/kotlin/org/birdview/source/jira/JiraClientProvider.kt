package org.birdview.source.jira

import org.birdview.storage.BVJiraConfig
import javax.inject.Named

@Named
class JiraClientProvider {
    fun getJiraClient(jiraConfig: BVJiraConfig) =
            JiraClient(jiraConfig)
}