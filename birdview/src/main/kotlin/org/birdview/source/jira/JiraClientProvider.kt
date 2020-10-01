package org.birdview.source.jira

import org.birdview.config.sources.BVJiraConfig
import javax.inject.Named

@Named
class JiraClientProvider {
    fun getJiraClient(jiraConfig: BVJiraConfig) =
            JiraClient(jiraConfig)
}