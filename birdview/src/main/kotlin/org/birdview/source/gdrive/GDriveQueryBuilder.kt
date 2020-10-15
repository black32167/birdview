package org.birdview.source.gdrive

import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVUserSourceStorage
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class GDriveQueryBuilder (
        private val userConfigProvider: BVUserSourceStorage
) {
    fun getQuery(user: String, updatedPeriod: TimeIntervalFilter, sourceName: String): String? {
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

    private fun getUserClause(bvUser: String, sourceName: String): String? {
        val sourceUserName = getUser(bvUser, sourceName)
        return "('${sourceUserName}' in owners) or ('${sourceUserName}' in writers) or ('${bvUser}' in readers)"
    }

    private fun getUser(userAlias: String, sourceName: String): String =
            userConfigProvider.getSourceProfile(userAlias, sourceName).sourceUserName
}