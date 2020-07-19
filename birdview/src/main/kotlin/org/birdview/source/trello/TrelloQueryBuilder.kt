package org.birdview.source.trello

import org.birdview.config.BVTrelloConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.BVDocumentFilter
import org.birdview.model.ReportType
import org.birdview.model.UserFilter
import org.birdview.model.UserRole
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Named

@Named
class TrelloQueryBuilder(
        private val usersConfigProvider: BVUsersConfigProvider
) {
    fun getQueries(filter: BVDocumentFilter, trelloConfig: BVTrelloConfig): List<String> =
            filter.userFilters.mapNotNull { userClause(it, trelloConfig) }
            .map { userClause ->
                userClause +
                listOfNotNull(
                        getListClause(filter),
                        getEditedClause(filter)
                ).joinToString(" ") + " sort:edited"
            }

    private fun getListClause(filter: BVDocumentFilter): String? =
            getListNames(filter)?.joinToString (",") { " list:\"${it}\"" }

    private fun getListNames(filter: BVDocumentFilter): List<String>? = when(filter.reportType) {
        ReportType.WORKED, ReportType.LAST_DAY  -> listOf("Done", "Progress", "Blocked")
        ReportType.PLANNED -> listOf("Planned", "Progress", "Blocked", "Backlog")
    }

    private fun getEditedClause(filter: BVDocumentFilter): String? =
            filter.since?.let { "edited:${getDaysBackFromNow(it)}" }

    private fun getUser(userAlias: String?, sourceName:String): String =
            if (userAlias == null) "me"
            else usersConfigProvider.getUserName(userAlias, sourceName)

    private fun getDaysBackFromNow(since: ZonedDateTime): Int =
            ChronoUnit.DAYS.between(since, ZonedDateTime.now()).toInt()

    private fun userClause(filter: UserFilter, trelloConfig: BVTrelloConfig): String? = when (filter.role) {
        UserRole.IMPLEMENTOR -> "@${getUser(filter.userAlias, trelloConfig.sourceName)}"
        else -> null
    }
}