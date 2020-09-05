package org.birdview.source.github

import org.birdview.analysis.*
import org.birdview.config.BVGithubConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.github.GithubUtils.parseDate
import org.birdview.source.github.gql.model.GqlGithubEvent
import org.birdview.source.github.gql.model.GqlGithubPullRequest
import org.birdview.source.github.gql.model.GqlGithubReviewUser
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVFilters
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Named

@Named
open class GithubTaskService(
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        private val githubClientProvider: GithubClientProvider,
        private val githubQueryBuilder: GithubQueryBuilder
): BVTaskSource {
    companion object {
        const val GITHUB_ID = "githubId"
    }
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    override fun getTasks(user: String?, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit) {
        sourcesConfigProvider.getConfigsOfType(BVGithubConfig::class.java)
                .forEach { config -> getTasks(user, updatedPeriod, config, chunkConsumer) }
    }

    private fun getTasks(
            user: String?,
            updatedPeriod: TimeIntervalFilter,
            githubConfig:BVGithubConfig,
            chunkConsumer: (List<BVDocument>) -> Unit) {

        val gqlClient = githubClientProvider.getGithubGqlClient(githubConfig)
        val githubQuery = githubQueryBuilder.getFilterQueries(user, updatedPeriod, githubConfig)
        gqlClient.getPullRequests(githubQuery) { prs ->
            val docs = prs.map { pr -> toBVDocument(pr, githubConfig) }
            chunkConsumer.invoke(docs)
        }
    }

    private fun toBVDocument(pr: GqlGithubPullRequest, githubConfig: BVGithubConfig): BVDocument {
        val description = pr.bodyText ?: ""
        val title = BVFilters.removeIdsFromText(pr.title)
        val operations = extractOperations(pr, sourceName = githubConfig.sourceName)
        val status = mapStatus(pr.state)
        return BVDocument(
                ids = setOf(BVDocumentId(id = pr.id, type = GITHUB_ID, sourceName = githubConfig.sourceName)),
                title = title,
                body = description,
                updated = parseDate(pr.updatedAt),
                created = parseDate(pr.createdAt),
                closed = extractClosed(operations, status),
                httpUrl = pr.url,
                refsIds = BVFilters.filterIdsFromText(pr.headRefName, pr.title, description),
                groupIds = setOf(),
                status = status,
                operations = operations,
                key = pr.url.replace(".*/".toRegex(), "#"),
                users = extractUsers(pr, githubConfig, operations)
        )
    }


    private fun extractClosed(operations: List<BVDocumentOperation>, status: BVDocumentStatus?): Date? =
            if (status == BVDocumentStatus.DONE) {
                operations.find { operation -> operation.description == "merged" } ?.created
            } else {
                null
            }

    private fun extractUsers(pr: GqlGithubPullRequest, config: BVGithubConfig, operations: List<BVDocumentOperation>): List<BVDocumentUser> {
        val creators = listOfNotNull(pr.author?.login)
        val implementors = creators +
                operations.filter { it.type == BVDocumentOperationType.COLLABORATE }.mapNotNull { it.author }
        val watchers = pr.assignees.nodes.map { it.login } +
                pr.reviewRequests.nodes.mapNotNull { (it.requestedReviewer as? GqlGithubReviewUser) ?.login }

        return creators.mapNotNull { mapDocumentUser(it, config.sourceName, UserRole.CREATOR) } +
                implementors.mapNotNull{ mapDocumentUser(it, config.sourceName, UserRole.IMPLEMENTOR) } +
                watchers.mapNotNull { mapDocumentUser(it, config.sourceName, UserRole.WATCHER) }
    }

    private fun mapDocumentUser(user: String?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            user ?.let { user -> BVDocumentUser(userName = user, sourceName = sourceName, role = userRole) }

    private fun mapStatus(state: String): BVDocumentStatus? = when (state.toLowerCase()) {
        "open" -> BVDocumentStatus.PROGRESS
        "closed", "merged" -> BVDocumentStatus.DONE
        else -> throw IllegalArgumentException("unknown PR state:${state}")
    }

    private fun extractOperations(pr: GqlGithubPullRequest, sourceName: String): List<BVDocumentOperation> =
        pr.timelineItems.nodes
                .mapNotNull { toOperation(it as GqlGithubEvent, sourceName) }
                .reversed()

    private fun toOperation(event: GqlGithubEvent, sourceName: String): BVDocumentOperation? =
            event
                    .takeIf {  it.timestamp!= null && it.user != null }
                    ?.let { event ->
                        BVDocumentOperation(
                                description = "commit",
                                author = event.user!!,
                                created = parseDate(event.timestamp!!),
                                sourceName = sourceName,
                                type = event.contributionType
                        )
                    }

    override fun getType() = SourceType.GITHUB

    override fun isAuthenticated(sourceName: String): Boolean =
            sourcesConfigProvider.getConfigByName(sourceName, BVGithubConfig::class.java) != null

}