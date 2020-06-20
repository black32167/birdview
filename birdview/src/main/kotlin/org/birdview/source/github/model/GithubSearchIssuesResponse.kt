package org.birdview.source.github.model

class GithubSearchIssuesResponse (
    val total_count: Int,
    val items: Array<GithubIssue>
)