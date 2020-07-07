package org.birdview.source.github

import java.time.ZonedDateTime

class GithubIssuesFilter (
        val repository:String? = null,
        val prState: String? = null,
        val since: ZonedDateTime? = null,
        val userAlias:String?
)
