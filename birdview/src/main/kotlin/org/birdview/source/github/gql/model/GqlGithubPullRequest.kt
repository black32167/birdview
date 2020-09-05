package org.birdview.source.github.gql.model

class GqlGithubPullRequest (
        val id: String,
        val url: String,
        val title: String,
        val state: String,
        val bodyText: String?,
        val author: GqlGithubActor?,
        val updatedAt: String,
        val createdAt: String,
        val baseRefName: String,
        val headRefName: String,
        val reviewRequests: GqlGithubNodesCollection<GqlGithubReviewRequest>,
        val assignees: GqlGithubNodesCollection<GqlGithubUser>,
        val timelineItems: GqlGithubNodesCollection<GqlGithubEvent>
)