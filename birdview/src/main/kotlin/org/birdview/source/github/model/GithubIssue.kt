package org.birdview.source.github.model

class GithubIssue (
        val id: String,
        val pull_request : GithubPullRequestRef?,
        val comments_url: String,
        val events_url: String,
        var body: String?,
        var title: String,
        val updated_at : String,
        val created_at : String,
        val state: String
)

class GithubPullRequestRef (
        val url: String,
        var html_url: String
)