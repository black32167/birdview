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
                        getUpdateAfterClause(updatedPeriod.after),
                        getUpdateBeforeClause(updatedPeriod.before)
                ).joinToString(" ")

    private fun getUpdateAfterClause(after: ZonedDateTime?):String? =
            after?.let { "updated:>=${it.format(DateTimeFormatter.ISO_LOCAL_DATE)}" }

    private fun getUpdateBeforeClause(before: ZonedDateTime?):String? =
            before?.let { "updated:<${it.format(DateTimeFormatter.ISO_LOCAL_DATE)}" }

    private fun userClause(userAlias: String, githubConfig: BVGithubConfig): String? {
        val user = getGithubUser(userAlias, githubConfig)
        return "involves:${user}"
    }

    private fun getGithubUser(bvUser: String, githubConfig: BVGithubConfig): String =
            userSourceStorage.getSourceProfile(bvUser, githubConfig.sourceName).sourceUserName
}