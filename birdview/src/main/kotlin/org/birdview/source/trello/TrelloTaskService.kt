package org.birdview.source.trello

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentUser
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.trello.model.TrelloCard
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import javax.inject.Named

@Named
open class TrelloTaskService(
        private val trelloClient: TrelloClient,
        private val trelloQueryBuilder: TrelloQueryBuilder,
) : BVTaskSource {
    companion object {
        private const val TRELLO_CARD_SHORTLINK_TYPE = "trelloCardShortLink"
        const val TRELLO_BOARD_TYPE = "trelloBoardId"
        private const val TRELLO_LABEL_TYPE = "trelloLabel"
        private const val TRELLO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    }

    override fun getTasks(
        bvUser: String,
        updatedPeriod: TimeIntervalFilter,
        sourceConfig: BVSourceConfigProvider.SyntheticSourceConfig,
        chunkConsumer: BVSessionDocumentConsumer
    ) {
        val sourceUserName = sourceConfig.sourceUserName
        val query = trelloQueryBuilder.getQueries(sourceUserName, updatedPeriod)

        trelloClient.getCards(
            bvUser = bvUser,
            sourceName = sourceConfig.sourceName,
            query = query) { cards->
            val listsMap = trelloClient.loadLists(
                bvUser = bvUser,
                sourceName = sourceConfig.sourceName,
                listsIds = cards.map { it.idList  })
                    .associateBy { it.id }

            val tasks = cards.map { card ->
                BVDocument(
                        ids = extractIds(card),
                        title = card.name,
                        key = "#${card.id}",
                        body = card.desc,
                        updated = parseDate(card.dateLastActivity),
                        created = parseDate(card.dateLastActivity),
                        httpUrl = card.url,
                        users = extractUsers(card, sourceConfig.sourceName),
                        refs = BVFilters.filterRefsFromText("${card.desc} ${card.name}")
                                .map { BVDocumentRef(it) },
                        status = mapStatus(listsMap[card.idList]?.name ?: ""),
                        // TODO: load user by id to infer user name!
                        sourceType = getType(),
                        sourceName = sourceConfig.sourceName
                )
            }

            chunkConsumer.consume(tasks)
        }
    }

    private fun extractUsers(card: TrelloCard, sourceName: String): List<BVDocumentUser> =
            card.idMembers.flatMap { listOf(
                    BVDocumentUser(userName = it, role = UserRole.IMPLEMENTOR, sourceName = sourceName),
                    BVDocumentUser(userName = it, role = UserRole.WATCHER, sourceName = sourceName)
            )}

    private fun mapStatus(state: String): BVDocumentStatus? = when (state) {
        "Done" -> BVDocumentStatus.DONE
        "Progress", "In Progress", "In Review", "Blocked" -> BVDocumentStatus.PROGRESS
        "To Do", "Planned" -> BVDocumentStatus.PLANNED
        "Backlog" -> BVDocumentStatus.BACKLOG
        else -> BVDocumentStatus.BACKLOG
    }

    override fun getType() = SourceType.TRELLO

    private fun extractIds(card: TrelloCard): Set<BVDocumentId> =
            setOf(
                    BVDocumentId(id = card.id),
                    BVDocumentId(id = card.shortLink))

    private fun extractGroupIds(card: TrelloCard): Set<BVDocumentId> =
            setOf(BVDocumentId(card.idBoard)) +
                    card.labels.map { BVDocumentId(it.id) }

    private fun parseDate(dateString: String) = BVDateTimeUtils.parse(dateString, TRELLO_DATETIME_PATTERN)
}
