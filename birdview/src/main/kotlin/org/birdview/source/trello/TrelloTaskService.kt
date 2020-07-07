package org.birdview.source.trello

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.tokenize.TextTokenizer
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVTrelloConfig
import org.birdview.model.ReportType
import org.birdview.request.TasksRequest
import org.birdview.source.BVTaskSource
import org.birdview.source.trello.model.TrelloCard
import org.birdview.utils.BVFilters
import java.util.*
import javax.inject.Named

@Named
class TrelloTaskService(
        val sourcesConfigProvider: BVSourcesConfigProvider,
        val tokenizer: TextTokenizer,
        private val trelloClientProvider: TrelloClientProvider
) : BVTaskSource {
    companion object {
        val TRELLO_CARD_ID_TYPE = "trelloCardId"
        val TRELLO_CARD_SHORTLINK_TYPE = "trelloCardShortLink"
        val TRELLO_BOARD_TYPE = "trelloBoardId"
        val TRELLO_LABEL_TYPE = "trelloLabel"
    }
    //private val dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME//java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")2020-04-29T04:12:34.125Z
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    // TODO: parallelize
    override fun getTasks(request: TasksRequest): List<BVDocument> =
            sourcesConfigProvider.getConfigsOfType(BVTrelloConfig::class.java)
                    .flatMap { config-> getTasks(request, config) }

    private fun getTasks(request: TasksRequest, trelloConfig: BVTrelloConfig): List<BVDocument> {
        val cards = trelloClientProvider.getTrelloClient(trelloConfig).getCards(getCardsFilter(request))

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
                status = listsMap[card.idList]?.name
            )
        }
        return tasks
    }

    private fun getCardsFilter(request: TasksRequest): TrelloCardsFilter = when(request.reportType) {
        ReportType.WORKED -> TrelloCardsFilter(
                since = request.since,
                user = request.user,
                listNames = listOf("Done", "Progress", "Blocked"))
        ReportType.PLANNED -> TrelloCardsFilter(
                user = request.user,
                listNames = listOf("Planned", "Progress", "Blocked", "Backlog"))
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
