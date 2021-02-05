package org.birdview.source.trello

import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVTrelloConfig
import org.birdview.storage.BVUserSourceStorage
import java.time.OffsetDateTime
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

    private fun getUpdateAfterClause(after: OffsetDateTime?): String? =
            after?.let { "edited:${getDaysBackFromNow(it)}" }

    private fun getUpdateBeforeClause(before: OffsetDateTime?): String? =
            before?.let { "-edited:${getDaysBackFromNow(it)}" }

    private fun getDaysBackFromNow(since: OffsetDateTime): Int =
            ChronoUnit.DAYS.between(since, OffsetDateTime.now()).toInt()

    private fun userClause(bvUser: String, trelloConfig: BVTrelloConfig): String? =
            "@${userSourceStorage.getSourceProfile(bvUser, trelloConfig.sourceName).sourceUserName}"
}