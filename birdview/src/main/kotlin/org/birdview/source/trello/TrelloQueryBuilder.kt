package org.birdview.source.trello

import org.birdview.config.BVTrelloConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.UserFilter
import org.birdview.model.UserRole
import javax.inject.Named

@Named
class TrelloQueryBuilder(
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getQueries(userFilters: List<UserFilter>, trelloConfig: BVTrelloConfig): List<String> =
            userFilters
                    .mapNotNull { userClause(it, trelloConfig) }
                    .map { userClause -> "$userClause sort:edited" }

    private fun getUser(userAlias: String?, sourceName:String): String =
            if (userAlias == null) "me"
            else usersConfigProvider.getUserName(userAlias, sourceName)

    private fun userClause(filter: UserFilter, trelloConfig: BVTrelloConfig): String? = when (filter.role) {
        UserRole.IMPLEMENTOR -> "@${getUser(filter.userAlias, trelloConfig.sourceName)}"
        else -> null
    }
}