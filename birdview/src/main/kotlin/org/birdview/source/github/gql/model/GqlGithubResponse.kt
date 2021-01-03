package org.birdview.source.github.gql.model

import org.birdview.gql.model.GqlError

open class GqlGithubResponse<T> (
        val data: T?,
        val errors: Array<GqlError> = emptyArray()
)

class GqlGithubSearchPullRequestResponse(
        data: GqlGithubSearchContainer<GqlGithubPullRequest>?
): GqlGithubResponse<GqlGithubSearchContainer<GqlGithubPullRequest>>(data)

class GqlGithubSearchUserResponse(
        data: GqlGithubSearchContainer<GqlGithubUser>?
): GqlGithubResponse<GqlGithubSearchContainer<GqlGithubUser>>(data)

class GqlGithubSearchContainer<T> (
        val search: GqlGithubSearch<T>
)

class GqlGithubSearch<T> (
        val edges: Array<GqlGithubEdge<T>>,
        val pageInfo: GqlGithubPageInfo
)
