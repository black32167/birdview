package org.birdview.source.github

import org.birdview.analysis.*
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVDocumentsRelation
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.github.GithubUtils.parseDate
import org.birdview.source.github.gql.model.GqlGithubEvent
import org.birdview.source.github.gql.model.GqlGithubPullRequest
import org.birdview.source.github.gql.model.GqlGithubReviewUser
import org.birdview.source.github.gql.model.GqlGithubUserActor
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVGithubConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.utils.BVFilters
import java.util.*
import javax.inject.Named

@Named
open class GithubTaskService(
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val githubClientProvider: GithubClientProvider,
        private val githubQueryBuilder: GithubQueryBuilder,
        private val secretsStorage: BVSourceSecretsStorage
): BVTaskSource {
    override fun getTasks(
            bvUser: String,
            updatedPeriod: TimeIntervalFilter,
            sourceConfig: BVAbstractSourceConfig,
            chunkConsumer: (List<BVDocument>) -> Unit) {
        val githubConfig = sourceConfig as BVGithubConfig
        val gqlClient = githubClientProvider.getGithubGqlClient(githubConfig)
        val githubQuery = githubQueryBuilder.getFilterQueries(bvUser, updatedPeriod, githubConfig)
        gqlClient.getPullRequests(githubQuery) { prs ->
            val docs = prs.map { pr -> toBVDocument(pr, githubConfig) }
            chunkConsumer.invoke(docs)
        }
    }

    override fun resolveSourceUserId(sourceName:String, email: String):String {
        val githubConfig = secretsStorage.getConfigByName(sourceName, BVGithubConfig::class.java)!!
        val client = githubClientProvider.getGithubGqlClient(githubConfig)
        val userName = client.getUserByEmail(email)
        return userName ?: email
    }

    private fun toBVDocument(pr: GqlGithubPullRequest, githubConfig: BVGithubConfig): BVDocument {
        val description = pr.bodyText ?: ""
        val title = BVFilters.removeIdsFromText(pr.title)
        val operations = extractOperations(pr, sourceName = githubConfig.sourceName)
        val status = mapStatus(pr.state)
        return BVDocument(
                ids = setOf(BVDocumentId(id = pr.id)),
                title = title,
                key = pr.url.replace(".*/".toRegex(), "#"),
                body = description,
                updated = parseDate(pr.updatedAt),
                created = parseDate(pr.createdAt),
                closed = extractClosed(operations, status),
                httpUrl = pr.url,
                users = extractUsers(pr, githubConfig, operations),
                refs = BVFilters.filterRefsFromText(pr.headRefName, pr.title, description)
                        .map {
                            BVDocumentRef(it, BVDocumentsRelation.getHierarchyRelationType(SourceType.GITHUB, it.sourceType))
                         },
                status = status,
                operations = operations,
                sourceType = getType(),
                sourceName = githubConfig.sourceName
        )
    }

    private fun extractClosed(operations: List<BVDocumentOperation>, status: BVDocumentStatus?): Date? =
            if (status == BVDocumentStatus.DONE) {
                operations.find { operation -> operation.description == "merged" } ?.created
            } else {
                null
            }

    private fun extractUsers(pr: GqlGithubPullRequest, config: BVGithubConfig, operations: List<BVDocumentOperation>): List<BVDocumentUser> =
            ((pr.author as? GqlGithubUserActor)?.login ?.let {
                listOfNotNull(mapDocumentUser(it, config.sourceName, UserRole.IMPLEMENTOR)) } ?: emptyList()) +
            pr.assignees.nodes.mapNotNull { mapDocumentUser(it.login, config.sourceName, UserRole.WATCHER) } +
            pr.reviewRequests.nodes.mapNotNull { mapDocumentUser((it.requestedReviewer as? GqlGithubReviewUser) ?.login, config.sourceName, UserRole.WATCHER) } +
            operations.mapNotNull {
                when(it.type) {
                    BVDocumentOperationType.UPDATE -> mapDocumentUser(it.author, config.sourceName, UserRole.IMPLEMENTOR)
                    BVDocumentOperationType.COMMENT,BVDocumentOperationType.NONE -> mapDocumentUser(it.author, config.sourceName, UserRole.WATCHER)
                }
            }

    private fun mapDocumentUser(user: String?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            user ?.let { BVDocumentUser(userName = it, sourceName = sourceName, role = userRole) }

    private fun mapStatus(state: String): BVDocumentStatus? = when (state.toLowerCase()) {
        "open" -> BVDocumentStatus.PROGRESS
        "closed", "merged" -> BVDocumentStatus.DONE
        else -> throw IllegalArgumentException("unknown PR state:${state}")
    }

    private fun extractOperations(pr: GqlGithubPullRequest, sourceName: String): List<BVDocumentOperation> =
        pr.timelineItems.nodes
                .mapNotNull { toOperation(it, sourceName) }
                .reversed()

    private fun toOperation(event: GqlGithubEvent, sourceName: String): BVDocumentOperation? =
            event
                    .takeIf {  it.timestamp!= null && it.user != null }
                    ?.let {
                        BVDocumentOperation(
                                description = event.type,
                                author = event.user!!,
                                created = parseDate(event.timestamp!!),
                                sourceName = sourceName,
                                type = event.contributionType
                        )
                    }

    override fun getType() = SourceType.GITHUB

    override fun isAuthenticated(sourceName: String): Boolean =
            sourceSecretsStorage.getConfigByName(sourceName, BVGithubConfig::class.java) != null

}