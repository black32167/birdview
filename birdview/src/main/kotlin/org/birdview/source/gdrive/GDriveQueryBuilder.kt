package org.birdview.source.gdrive

import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.BVDocumentFilter
import org.birdview.model.UserFilter
import org.birdview.model.UserRole
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class GDriveQueryBuilder (
        private val userConfigProvider: BVUsersConfigProvider
) {
    fun getQuery(userFilters: List<UserFilter>, sourceName: String): String? {
        val userClause = getUserClause(userFilters, sourceName)
        return if (userClause.isBlank()) {
            null
        } else {
            listOfNotNull(
                    userClause,
                    "mimeType='application/vnd.google-apps.document'"
            ).joinToString(" AND ")
        }
    }

    private fun getModifiedTimeClause(filter: BVDocumentFilter): String? =
            filter.since?.let { since->
                "modifiedTime>'${since.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))}'" }

    private fun getUserClause(userFilters: List<UserFilter>, sourceName: String): String =
        userFilters
                .mapNotNull { getUserClause(it, sourceName) }
                .joinToString(" AND ")

    private fun getUserClause(filter: UserFilter, sourceName: String): String? = when (filter.role) {
        UserRole.IMPLEMENTOR -> "'${getUser(filter.userAlias, sourceName)}' in owners"
        else -> null
    }

    private fun getUser(userAlias: String?, sourceName: String): String =
            userAlias?.let { userConfigProvider.getUserName(userAlias, sourceName) } ?: "me"
}