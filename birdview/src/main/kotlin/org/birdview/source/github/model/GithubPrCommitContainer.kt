package org.birdview.source.github.model

class GithubPrCommitContainer (
        val author: GithubUser?,
        val committer: GithubUser?,
        val commit: GithubPrCommit
)

class GithubPrCommit (
        val author: GithubPrCommitUser,
        val committer: GithubPrCommitUser?
)

class GithubPrCommitUser (
        val name: String,
        val email: String?,
        val date: String
)