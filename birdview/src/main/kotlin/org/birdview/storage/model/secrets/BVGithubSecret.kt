package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

class BVGithubSecret (
        sourceName: String = "github",
        user: String,
        val token: String
): BVAbstractSourceSecret(SourceType.GITHUB, sourceName, user) {
    val baseGqlUrl = "https://api.github.com/graphql"
    val baseUrl = "https://api.github.com"
}