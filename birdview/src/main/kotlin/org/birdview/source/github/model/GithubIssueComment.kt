package org.birdview.source.github.model

import org.birdview.source.github.gql.model.GqlGithubActor

class GithubIssueComment (
        val body: String,
        val user: GqlGithubActor,
        val created_at: String,
        val updated_at: String
)