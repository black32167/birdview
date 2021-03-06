package org.birdview.source.github.gql.model

class GqlGithubCommit (
        val committer: GqlGithubGitActor?,
        val author: GqlGithubGitActor?,
        val committedDate: String,
        val pushedDate: String?
)