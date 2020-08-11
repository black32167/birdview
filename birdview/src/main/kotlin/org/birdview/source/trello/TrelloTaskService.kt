package org.birdview.source.trello

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVTrelloConfig
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVTaskSource
import org.birdview.source.trello.model.TrelloCard
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import javax.inject.Named

@Named
open class TrelloTaskService(
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        private val trelloClientProvider: TrelloClientProvider,
        private val trelloQueryBuilder: TrelloQueryBuilder
) : BVTaskSource {
    companion object {
        private const val TRELLO_CARD_ID_TYPE = "trelloCardId"
        private const val TRELLO_CARD_SHORTLINK_TYPE = "trelloCardShortLink"
        const val TRELLO_BOARD_TYPE = "trelloBoardId"
        private const val TRELLO_LABEL_TYPE = "trelloLabel"
        private const val TRELLO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

    override fun getTasks(user: String?, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit): Unit =
                sourcesConfigProvider.getConfigsOfType(BVTrelloConfig::class.java)
                    .forEach { config-> getTasks(user, updatedPeriod, config, chunkConsumer) }

    private fun getTasks(
            user: String?,
            updatedPeriod: TimeIntervalFilter,
            trelloConfig: BVTrelloConfig,
            chunkConsumer: (List<BVDocument>) -> Unit) {
        val query = trelloQueryBuilder.getQueries(user, updatedPeriod, trelloConfig)

        trelloClientProvider.getTrelloClient(trelloConfig).getCards(query) { cards->
            val listsMap = trelloClientProvider.getTrelloClient(trelloConfig)
                    .loadLists(cards.map { it.idList  })
                    .associateBy { it.id }

            val tasks = cards.map { card ->
                BVDocument(
                        ids = extractIds(card, trelloConfig.sourceName),
                        title = card.name,
                        updated = parseDate(card.dateLastActivity),
                        created = parseDate(card.dateLastActivity),
                        httpUrl = card.url,
                        body = card.desc,
                        refsIds = BVFilters.filterIdsFromText("${card.desc} ${card.name}"),
                        groupIds = extractGroupIds(card, trelloConfig.sourceName),
                        status = mapStatus(listsMap[card.idList]?.name ?: ""),
                        key = "#${card.id}"
                )
            }

            chunkConsumer.invoke(tasks)
        }
    }

    private fun mapStatus(state: String): BVDocumentStatus? = when (state) {
        "Done" -> BVDocumentStatus.DONE
        "Progress", "In Progress", "In Review", "Blocked" -> BVDocumentStatus.PROGRESS
        "To Do", "Planned" -> BVDocumentStatus.PLANNED
        "Backlog" -> BVDocumentStatus.BACKLOG
        else -> null
    }

    override fun getType() = "trello"

    override fun isAuthenticated(sourceName: String): Boolean =
            sourcesConfigProvider.getConfigByName(sourceName, BVTrelloConfig::class.java) != null

    private fun extractIds(card: TrelloCard, sourceName: String): Set<BVDocumentId> =
            setOf(
                    BVDocumentId( id = card.id, type = TRELLO_CARD_ID_TYPE, sourceName = sourceName),
                    BVDocumentId( id = card.shortLink, type = TRELLO_CARD_SHORTLINK_TYPE, sourceName = sourceName))

    private fun extractGroupIds(card: TrelloCard, sourceName: String): Set<BVDocumentId> =
            setOf(BVDocumentId(card.idBoard, TRELLO_BOARD_TYPE, sourceName)) +
                    card.labels.map { BVDocumentId(it.id, TRELLO_LABEL_TYPE, sourceName) }

    private fun parseDate(dateString: String) = BVDateTimeUtils.parse(dateString, TRELLO_DATETIME_PATTERN)
}
