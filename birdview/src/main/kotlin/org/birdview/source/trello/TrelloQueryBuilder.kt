package org.birdview.source.trello

import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVTrelloConfig
import org.birdview.storage.BVUserSourceStorage
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Named

@Named
class TrelloQueryBuilder(
        private val userSourceStorage: BVUserSourceStorage
) {
    fun getQueries(user: String, updatedPeriod: TimeIntervalFilter, trelloConfig: BVTrelloConfig): String =
            listOfNotNull(
                    userClause(user, trelloConfig),
                    getUpdateAfterClause(updatedPeriod.after),
                    getUpdateBeforeClause(updatedPeriod.before)
            ).joinToString(" ") + " sort:edited"

    private fun getUpdateAfterClause(after: ZonedDateTime?): String? =
            after?.let { "edited:${getDaysBackFromNow(it)}" }

    private fun getUpdateBeforeClause(before: ZonedDateTime?): String? =
            before?.let { "-edited:${getDaysBackFromNow(it)}" }

    private fun getDaysBackFromNow(since: ZonedDateTime): Int =
            ChronoUnit.DAYS.between(since, ZonedDateTime.now()).toInt()

    private fun userClause(bvUser: String, trelloConfig: BVTrelloConfig): String? =
            "@${userSourceStorage.getSourceProfile(bvUser, trelloConfig.sourceName).sourceUserName}"
}