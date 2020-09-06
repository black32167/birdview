package org.birdview.source.github.gql.model

import org.birdview.gql.model.GqlError

open class GqlGithubResponse<T> (
        val data: T?,
        val errors: Array<GqlError> = emptyArray()
)

class GqlGithubSearchContainer<T> (
        val search: GqlGithubSearch<T>
)

class GqlGithubSearch<T> (
        val edges: Array<GqlGithubEdge<T>>,
        val pageInfo: GqlGithubPageInfo
)
