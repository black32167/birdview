package org.birdview.source.github

import org.birdview.config.BVGithubConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.UserFilter
import org.birdview.model.UserRole
import javax.inject.Named

@Named
class GithubQueryBuilder(
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getFilterQueries(userFilters: List<UserFilter>, githubConfig: BVGithubConfig): List<String> =
            userFilters
                    .mapNotNull { userClause(it, githubConfig) }
                    .map { userClause -> "type:pr $userClause" }

    private fun userClause(filter: UserFilter, githubConfig: BVGithubConfig): String? = when (filter.role) {
        UserRole.IMPLEMENTOR -> "author:${getGithubUser(filter.userAlias, githubConfig)}"
        else -> null
    }

    private fun getGithubUser(userAlias: String?, githubConfig: BVGithubConfig): String? =
            if (userAlias == null) "@me"
            else usersConfigProvider.getUserName(userAlias, githubConfig.sourceName)
}