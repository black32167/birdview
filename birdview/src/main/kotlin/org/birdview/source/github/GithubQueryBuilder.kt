package org.birdview.source.github

import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVGithubConfig
import org.birdview.storage.BVUserSourceStorage
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class GithubQueryBuilder(
        private val userSourceStorage: BVUserSourceStorage
) {
    fun getFilterQueries(user: String, updatedPeriod: TimeIntervalFilter, githubConfig: BVGithubConfig): String =
                listOfNotNull(
                        "type:pr ",
                        userClause(user, githubConfig),
                        getUpdatePeriodClause(updatedPeriod.after!!, updatedPeriod.before!!)
                ).joinToString(" ")

    private fun getUpdatePeriodClause(after: ZonedDateTime, before: ZonedDateTime):String? =
            "updated:${after.format(DateTimeFormatter.ISO_LOCAL_DATE)}..${before.format(DateTimeFormatter.ISO_LOCAL_DATE)}"

    private fun userClause(userAlias: String, githubConfig: BVGithubConfig): String? {
        val user = getGithubUser(userAlias, githubConfig)
        return "involves:${user}"
    }

    private fun getGithubUser(bvUser: String, githubConfig: BVGithubConfig): String =
            userSourceStorage.getSourceProfile(bvUser, githubConfig.sourceName).sourceUserName
}