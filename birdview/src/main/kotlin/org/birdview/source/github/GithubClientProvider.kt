package org.birdview.source.github

import org.birdview.config.BVGithubConfig
import javax.inject.Named

@Named
class GithubClientProvider {
    fun getGithubClient(githubConfig: BVGithubConfig) =
            GithubClient(githubConfig)
}