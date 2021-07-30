package org.birdview.source.trello

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentUser
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.trello.model.TrelloCard
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVTrelloConfig
import org.birdview.storage.BVUserSourceStorage
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import javax.inject.Named

@Named
open class TrelloTaskService(
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val trelloClient: TrelloClient,
        private val trelloQueryBuilder: TrelloQueryBuilder,
        private val userSourceStorage: BVUserSourceStorage,
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
        sourceConfig: BVAbstractSourceConfig,
        chunkConsumer: BVSessionDocumentConsumer
    ) {
        val trelloConfig = sourceConfig as BVTrelloConfig
        val sourceUserName = userSourceStorage.getSourceProfile(bvUser, trelloConfig.sourceName).sourceUserName
        val query = trelloQueryBuilder.getQueries(sourceUserName, updatedPeriod)

        trelloClient.getCards(trelloConfig, query) { cards->
            val listsMap = trelloClient.loadLists(trelloConfig, cards.map { it.idList  })
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
                        users = extractUsers(card, trelloConfig.sourceName),
                        refs = BVFilters.filterRefsFromText("${card.desc} ${card.name}")
                                .map { BVDocumentRef(it) },
                        status = mapStatus(listsMap[card.idList]?.name ?: ""),
                        // TODO: load user by id to infer user name!
                        sourceType = getType(),
                        sourceName = trelloConfig.sourceName
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

    override fun isAuthenticated(sourceName: String): Boolean =
            sourceSecretsStorage.getConfigByName(sourceName, BVTrelloConfig::class.java) != null

    private fun extractIds(card: TrelloCard): Set<BVDocumentId> =
            setOf(
                    BVDocumentId(id = card.id),
                    BVDocumentId(id = card.shortLink))

    private fun extractGroupIds(card: TrelloCard): Set<BVDocumentId> =
            setOf(BVDocumentId(card.idBoard)) +
                    card.labels.map { BVDocumentId(it.id) }

    private fun parseDate(dateString: String) = BVDateTimeUtils.parse(dateString, TRELLO_DATETIME_PATTERN)
}
