package org.birdview.source.github.model

class GithubPullRequest (
        val id: String,
        val updated_at : String,
        val created_at : String,
        val html_url: String,
        val title : String,
        val head: GithubBranch,
        val base: GithubBranch,
        val state: String,
        val body: String?
)
