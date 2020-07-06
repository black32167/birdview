package org.birdview.source.jira

import java.time.ZonedDateTime

class JiraIssuesFilter(
        val userAlias: String?,
        val issueStatuses: List<String>?,
        val since: ZonedDateTime
)