package org.birdview.source.gdrive

import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.TimeIntervalFilter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class GDriveQueryBuilder (
        private val userConfigProvider: BVUsersConfigProvider
) {
    fun getQuery(user: String?, updatedPeriod: TimeIntervalFilter, sourceName: String): String? {
        val userClause = getUserClause(user, sourceName)
        return listOfNotNull(
                userClause,
                "mimeType='application/vnd.google-apps.document'",
                getModifiedAfterTimeClause(updatedPeriod.after),
                getModifiedBeforeTimeClause(updatedPeriod.before)
        ).joinToString(" AND ")
    }

    private fun getModifiedAfterTimeClause(after: ZonedDateTime?): String? =
            after?.let { "modifiedTime>='${formatDate(it)}'" }


    private fun getModifiedBeforeTimeClause(before: ZonedDateTime?): String? =
            before?.let { "modifiedTime<'${formatDate(it)}'" }

    private fun formatDate(date: ZonedDateTime) =
            date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))

    private fun getUserClause(user: String?, sourceName: String): String? =
        "'${getUser(user, sourceName)}' in owners"

    private fun getUser(userAlias: String?, sourceName: String): String =
            userAlias?.let { userConfigProvider.getUserName(userAlias, sourceName) } ?: "me"
}