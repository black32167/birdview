package org.birdview.source.trello

import org.birdview.config.sources.BVTrelloConfig
import org.birdview.config.user.BVUserProfileStorage
import org.birdview.model.TimeIntervalFilter
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Named

@Named
class TrelloQueryBuilder(
        private val userProfileStorage: BVUserProfileStorage
) {
    fun getQueries(user: String?, updatedPeriod: TimeIntervalFilter, trelloConfig: BVTrelloConfig): String =
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

    private fun getUser(userAlias: String?, sourceName:String): String =
            if (userAlias == null) "me"
            else userProfileStorage.getUserName(userAlias, sourceName)

    private fun userClause(user: String?, trelloConfig: BVTrelloConfig): String? =
            "@${getUser(user, trelloConfig.sourceName)}"
}