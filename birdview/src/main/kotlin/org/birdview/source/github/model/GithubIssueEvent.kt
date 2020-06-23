package org.birdview.source.github.model

class GithubIssueEvent (
        val id: Long,
        val actor: GithubUser,
        val event: String,
        val created_at: String
)