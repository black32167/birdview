package org.birdview.source.github

import org.birdview.model.TimeIntervalFilter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class GithubQueryBuilder() {
    fun getFilterQueries(sourceUserName: String, updatedPeriod: TimeIntervalFilter): String =
                listOfNotNull(
                        "type:pr ",
                        userClause(sourceUserName),
                        getUpdatePeriodClause(updatedPeriod.after, updatedPeriod.before)
                ).joinToString(" ")

    private fun getUpdatePeriodClause(after: OffsetDateTime?, before: OffsetDateTime?):String? =
        if (after != null && before != null) {
            "updated:${format(after)}..${format(before)}"
        } else if (after != null) {
            "updated:>${format(after)}"
        } else if (before != null) {
            "updated:<=${format(before)}"
        } else {
            null
        }

    private fun format(time: OffsetDateTime) = time.format(DateTimeFormatter.ISO_LOCAL_DATE)


    private fun userClause(sourceUserName: String): String {
        return "involves:${sourceUserName}"
    }
}