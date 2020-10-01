package org.birdview.source.github

import org.birdview.config.sources.BVGithubConfig
import org.birdview.source.github.gql.GithubGqlClient
import javax.inject.Named

@Named
class GithubClientProvider {
    fun getGithubClient(githubConfig: BVGithubConfig) =
            GithubClient(githubConfig)
    fun getGithubGqlClient(githubConfig: BVGithubConfig) =
            GithubGqlClient(githubConfig)
}