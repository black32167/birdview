package org.birdview.source.github

import java.time.ZonedDateTime

class GithubIssuesFilter (
        val repository:String? = null,
        val prState: String,
        val since: ZonedDateTime?,
        val userAlias:String?
)
