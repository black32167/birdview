package org.birdview.source.jira

import org.birdview.config.BVJiraConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.UserFilter
import org.birdview.model.UserRole
import javax.inject.Named

@Named
class JqlBuilder(
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getJql(userFilters: List<UserFilter>, jiraConfig: BVJiraConfig): String? =
            getUserJqlClause(userFilters, jiraConfig)
                .takeUnless { it.isBlank() }
                ?.let { userClause -> "$userClause order by updatedDate DESC" }

    private fun getUserJqlClause(userFilters: List<UserFilter>, jiraConfig: BVJiraConfig): String = userFilters
            .joinToString(" or ", "(", ")", transform = {
                filter -> getUserJqlClause(filter, jiraConfig)})

    private fun getUserJqlClause(userFilter: UserFilter, jiraConfig: BVJiraConfig): String = when (userFilter.role) {
        UserRole.CREATOR -> "creator"
        UserRole.IMPLEMENTOR -> "assignee"
        UserRole.WATCHER -> "watcher"
    } + " = ${getUser(userFilter.userAlias, jiraConfig)}"


    private fun getUser(userAlias: String?, jiraConfig: BVJiraConfig): String =
            if(userAlias == null) { "currentUser()" }
            else { "\"${usersConfigProvider.getUserName(userAlias, jiraConfig.sourceName)}\""}
}