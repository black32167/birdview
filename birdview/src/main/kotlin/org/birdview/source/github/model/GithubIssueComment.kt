package org.birdview.source.github.model

class GithubIssueComment (
        val body: String,
        val user: GithubUser,
        val created_at: String,
        val updated_at: String
)