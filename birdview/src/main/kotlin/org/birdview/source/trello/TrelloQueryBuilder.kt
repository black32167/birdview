package org.birdview.source.trello

import org.birdview.model.TimeIntervalFilter
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Named

@Named
class TrelloQueryBuilder {
    fun getQueries(sourceUserName: String, updatedPeriod: TimeIntervalFilter): String =
            listOfNotNull(
                sourceUserName,
                getUpdateAfterClause(updatedPeriod.after),
                getUpdateBeforeClause(updatedPeriod.before)
            ).joinToString(" ") + " sort:edited"

    private fun getUpdateAfterClause(after: OffsetDateTime?): String? =
            after?.let { "edited:${getDaysBackFromNow(it)}" }

    private fun getUpdateBeforeClause(before: OffsetDateTime?): String? =
            before?.let { "-edited:${getDaysBackFromNow(it)}" }

    private fun getDaysBackFromNow(since: OffsetDateTime): Int =
            ChronoUnit.DAYS.between(since, OffsetDateTime.now()).toInt()
}