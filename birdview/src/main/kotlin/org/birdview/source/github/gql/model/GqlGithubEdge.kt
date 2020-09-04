package org.birdview.source.github.gql.model

class GqlGithubEdge<T> (
        val cursor: String?,
        val node: T
)