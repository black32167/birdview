package org.birdview.source.gdrive

import org.birdview.model.TimeIntervalFilter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class GDriveQueryBuilder {
    fun getQuery(sourceUserName: String, updatedPeriod: TimeIntervalFilter): String {
        val userClause = getUserClause(sourceUserName)
        return listOfNotNull(
                "(${userClause})",
                "mimeType='application/vnd.google-apps.document'",
                getModifiedAfterTimeClause(updatedPeriod.after),
                getModifiedBeforeTimeClause(updatedPeriod.before)
        ).joinToString(" AND ")
    }

    private fun getModifiedAfterTimeClause(after: OffsetDateTime?): String? =
            after?.let { "modifiedTime>='${formatDate(it)}'" }


    private fun getModifiedBeforeTimeClause(before: OffsetDateTime?): String? =
            before?.let { "modifiedTime<'${formatDate(it)}'" }

    private fun formatDate(date: OffsetDateTime) =
            date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))

    private fun getUserClause(sourceUserName: String): String? {
        return "('${sourceUserName}' in owners) or ('${sourceUserName}' in writers) or ('${sourceUserName}' in readers)"
    }
}
