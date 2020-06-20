package org.birdview.source.jira

import java.time.ZonedDateTime

class JiraIssuesFilter(
        val userAlias: String?,
        val issueStatus: String?,
        val since: ZonedDateTime
)