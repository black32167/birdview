package org.birdview.source.trello

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.tokenize.TextTokenizer
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVTrelloConfig
import org.birdview.model.BVDocumentStatus
import org.birdview.model.UserFilter
import org.birdview.source.BVTaskSource
import org.birdview.source.trello.model.TrelloCard
import org.birdview.utils.BVFilters
import org.springframework.cache.annotation.Cacheable
import java.util.*
import javax.inject.Named

@Named
class TrelloTaskService(
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        val tokenizer: TextTokenizer,
        private val trelloClientProvider: TrelloClientProvider,
        private val trelloQueryBuilder: TrelloQueryBuilder
) : BVTaskSource {
    companion object {
        private const val TRELLO_CARD_ID_TYPE = "trelloCardId"
        private const val TRELLO_CARD_SHORTLINK_TYPE = "trelloCardShortLink"
        const val TRELLO_BOARD_TYPE = "trelloBoardId"
        private const val TRELLO_LABEL_TYPE = "trelloLabel"
    }
    //private val dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME//java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")2020-04-29T04:12:34.125Z
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    @Cacheable("bv")
    override fun getTasks(userFilters: List<UserFilter>): List<BVDocument> =
            sourcesConfigProvider.getConfigsOfType(BVTrelloConfig::class.java)
                    .flatMap { config-> getTasks(userFilters, config) }

    private fun getTasks(userFilters: List<UserFilter>, trelloConfig: BVTrelloConfig): List<BVDocument> {
        val cards = trelloQueryBuilder.getQueries(userFilters, trelloConfig)
                .flatMap { query ->
                    trelloClientProvider.getTrelloClient(trelloConfig).getCards(query) }

        val listsMap = trelloClientProvider.getTrelloClient(trelloConfig).loadLists(cards.map { it.idList  })
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
        return tasks
    }

    private fun mapStatus(state: String): BVDocumentStatus? = when (state) {
        "Done" -> BVDocumentStatus.DONE
        "Progress", "In Progress", "In Review", "Blocked" -> BVDocumentStatus.PROGRESS
        "To Do", "Planned" -> BVDocumentStatus.PLANNED
        "Backlog" -> BVDocumentStatus.BACKLOG
        else -> null
    }

    override fun getType() = "trello"

    private fun extractIds(card: TrelloCard, sourceName: String): Set<BVDocumentId> =
            setOf(
                    BVDocumentId( id = card.id, type = TRELLO_CARD_ID_TYPE, sourceName = sourceName),
                    BVDocumentId( id = card.shortLink, type = TRELLO_CARD_SHORTLINK_TYPE, sourceName = sourceName))

    private fun extractGroupIds(card: TrelloCard, sourceName: String): Set<BVDocumentId> =
            setOf(BVDocumentId(card.idBoard, TRELLO_BOARD_TYPE, sourceName)) +
                    card.labels.map { BVDocumentId(it.id, TRELLO_LABEL_TYPE, sourceName) }

    private fun parseDate(dateString: String):Date = dateTimeFormat.parse(dateString)//Date.parse(dateString, dateTimeFormat)
}
