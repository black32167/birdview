package org.birdview.source.github

import org.birdview.source.github.gql.GithubGqlClient
import org.birdview.storage.BVGithubConfig
import javax.inject.Named

@Named
class GithubClientProvider {
    fun getGithubClient(githubConfig: BVGithubConfig) =
            GithubClient(githubConfig)
    fun getGithubGqlClient(githubConfig: BVGithubConfig) =
            GithubGqlClient(githubConfig)
}