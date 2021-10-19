package org.birdview.source.github

import org.birdview.analysis.*
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.*
import org.birdview.source.github.GithubUtils.parseDate
import org.birdview.source.github.gql.GithubGqlClient
import org.birdview.source.github.gql.model.GqlGithubEvent
import org.birdview.source.github.gql.model.GqlGithubPullRequest
import org.birdview.source.github.gql.model.GqlGithubReviewUser
import org.birdview.source.github.gql.model.GqlGithubUserActor
import org.birdview.utils.BVDocumentUtils
import org.birdview.utils.BVFilters
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import javax.inject.Named

@Named
open class GithubTaskService(
    private val gqlClient: GithubGqlClient,
    private val githubQueryBuilder: GithubQueryBuilder,
): BVTaskSource {
    private val log = LoggerFactory.getLogger(GithubTaskService::class.java)

    override fun getTasks(
        bvUser: String,
        updatedPeriod: TimeIntervalFilter,
        sourceConfig: BVSourceConfigProvider.SyntheticSourceConfig,
        chunkConsumer: BVSessionDocumentConsumer
    ) {
        val sourceUserName = sourceConfig.sourceUserName
        val githubQuery = githubQueryBuilder.getFilterQueries(sourceUserName, updatedPeriod)
        gqlClient.getPullRequests(bvUser = bvUser, sourceName = sourceConfig.sourceName, githubQuery) { prs ->
            val docs = prs.map { pr -> toBVDocument(pr, sourceConfig) }
            chunkConsumer.consume(docs)
        }
    }

    override fun resolveSourceUserId(bvUser: String, sourceName: String, email: String):String {
        val userName = try {
            gqlClient.getUserByEmail(bvUser = bvUser, sourceName = sourceName, email = email)
        } catch (e: Throwable) {
            log.error("error resolving Github user by email", e)
            null
        }
        return userName ?: email
    }

    private fun toBVDocument(pr: GqlGithubPullRequest, githubConfig: BVSourceConfigProvider.SyntheticSourceConfig): BVDocument {
        val description = pr.bodyText ?: ""
        val title = BVFilters.removeIdsFromText(pr.title)
        val operations = extractOperations(pr, sourceName = githubConfig.sourceName)
        val status = mapStatus(pr.state)
        return BVDocument(
                ids = setOf(BVDocumentId(id = pr.id), BVDocumentId(id = pr.url)),
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
                            BVDocumentRef(it, BVDocumentNodesRelation.getHierarchyRelationType(SourceType.GITHUB, it.sourceType))
                         },
                status = status,
                operations = operations,
                sourceName = githubConfig.sourceName,
            internalId = BVDocumentUtils.hashId(pr.url)
        )
    }

    private fun extractClosed(operations: List<BVDocumentOperation>, status: BVDocumentStatus?): OffsetDateTime? =
            if (status == BVDocumentStatus.DONE) {
                operations.find { operation -> operation.description == "merged" } ?.created
            } else {
                null
            }

    private fun extractUsers(pr: GqlGithubPullRequest, config: BVSourceConfigProvider.SyntheticSourceConfig, operations: List<BVDocumentOperation>): List<BVDocumentUser> =
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
}