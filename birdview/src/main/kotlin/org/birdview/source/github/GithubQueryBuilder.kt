package org.birdview.source.github

import org.birdview.config.BVGithubConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.BVDocumentFilter
import org.birdview.model.ReportType
import org.birdview.model.UserFilter
import org.birdview.model.UserRole
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class GithubQueryBuilder(
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getFilterQueries(filter: BVDocumentFilter, githubConfig: BVGithubConfig): List<String> =
            filter.userFilters.mapNotNull { userClause(it, githubConfig) }
                    .map { userClause ->
                        "type:pr " +
                                listOfNotNull(
                                        prStateClause(filter),
                                        userClause,
                                        updatedTimeClause(filter)
                                ).joinToString(" ")
                    }

    private fun updatedTimeClause(filter: BVDocumentFilter):String? =
            filter.since
                    ?.let { "updated:>=${it.format(DateTimeFormatter.ISO_LOCAL_DATE)}" }

    private fun userClause(filter: UserFilter, githubConfig: BVGithubConfig): String? = when (filter.role) {
        UserRole.IMPLEMENTOR -> "author:${getGithubUser(filter.userAlias, githubConfig)}"
        else -> null
    }

    private fun prStateClause(filter: BVDocumentFilter): String? = when (filter.reportType) {
        ReportType.PLANNED -> "open"
        else -> null
    } ?.let { "state:${it}" }

    private fun getGithubUser(userAlias: String?, githubConfig: BVGithubConfig): String? =
            if (userAlias == null) "@me"
            else usersConfigProvider.getUserName(userAlias, githubConfig.sourceName)
}