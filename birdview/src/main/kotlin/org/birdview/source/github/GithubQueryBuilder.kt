package org.birdview.source.github

import org.birdview.config.sources.BVGithubConfig
import org.birdview.config.user.BVUserProfileStorage
import org.birdview.model.TimeIntervalFilter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class GithubQueryBuilder(
        private val userProfileStorage: BVUserProfileStorage
) {
    fun getFilterQueries(user: String?, updatedPeriod: TimeIntervalFilter, githubConfig: BVGithubConfig): String =
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

    private fun userClause(userAlias: String?, githubConfig: BVGithubConfig): String? {
        var user = getGithubUser(userAlias, githubConfig)
        return "involves:${user}"
    }


    private fun getGithubUser(userAlias: String?, githubConfig: BVGithubConfig): String? =
            if (userAlias == null) "@me"
            else userProfileStorage.getUserName(userAlias, githubConfig.sourceName)
}