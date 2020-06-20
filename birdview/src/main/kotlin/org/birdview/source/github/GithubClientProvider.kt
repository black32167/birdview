package org.birdview.source.github

import org.birdview.config.BVGithubConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.source.BVTaskListsDefaults
import javax.inject.Named

@Named
class GithubClientProvider(
        private val taskListDefaults: BVTaskListsDefaults,
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getGithubClient(githubConfig: BVGithubConfig) =
            GithubClient(githubConfig, taskListDefaults, usersConfigProvider)
}