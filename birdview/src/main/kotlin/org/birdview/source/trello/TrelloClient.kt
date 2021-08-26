package org.birdview.source.trello

import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.http.BVHttpSourceClientFactory
import org.birdview.source.trello.model.TrelloBoard
import org.birdview.source.trello.model.TrelloCard
import org.birdview.source.trello.model.TrelloCardsSearchResponse
import org.birdview.source.trello.model.TrelloList
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class TrelloClient(private val httpClientFactory: BVHttpSourceClientFactory) {
    private val log = LoggerFactory.getLogger(TrelloClient::class.java)
    private val cardsPerPage = 50

    fun getCards(trelloConfig: BVSourceConfigProvider.SyntheticSourceConfig, query:String, chunkConsumer: (List<TrelloCard>) -> Unit) {
        log.info("Running Trello query '{}'", query)
        var page = 0
        while (searchTrelloCards(trelloConfig, query, page, chunkConsumer)) {
            page++
        }
    }

    private fun searchTrelloCards(trelloConfig: BVSourceConfigProvider.SyntheticSourceConfig, query:String, page: Int, chunkConsumer: (List<TrelloCard>) -> Unit): Boolean {
        log.info("Loading trello issues page {} for query {}", page, query)
        val cards = searchTrelloCards(trelloConfig, query, page).cards.toList()
        return if(cards.isEmpty()) {
            false
        } else {
            chunkConsumer.invoke(cards)
            true
        }
    }

    fun getBoards(trelloConfig: BVSourceConfigProvider.SyntheticSourceConfig, boardIds: List<String>): List<TrelloBoard> =
        boardIds.map { boardId ->
            getTrello(
                trelloConfig = trelloConfig,
                resultClass = TrelloBoard::class.java,
                subPath = "boards/${boardId}")
        }

    fun loadLists(trelloConfig: BVSourceConfigProvider.SyntheticSourceConfig, listsIds: List<String>): List<TrelloList> = listsIds.map { listId ->
        getTrello(
            trelloConfig = trelloConfig,
            subPath = "lists/${listId}",
            resultClass = TrelloList::class.java
        )
    }

    private fun searchTrelloCards(trelloConfig: BVSourceConfigProvider.SyntheticSourceConfig, query: String, cardsPage: Int) =
        getTrello(
            trelloConfig = trelloConfig,
            resultClass = TrelloCardsSearchResponse::class.java,
            subPath = "search",
            parameters = mapOf(
                "query" to query,
                "partial" to true,
                //    "card_fields" to "idMembers,dateLastActivity,dateLastView,desc,descData,idOrganization,invitations,invited,labelNames,memberships,name,pinned,powerUps,prefs,shortLink,shortUrl,starred,subscribed,url",
                "cards_limit" to cardsPerPage,
                "cards_page" to cardsPage
            )
        )

    private fun <T> getTrello(trelloConfig: BVSourceConfigProvider.SyntheticSourceConfig, resultClass: Class<T>, subPath: String, parameters: Map<String, Any> = emptyMap()) =
        httpClientFactory.createClient(trelloConfig.sourceName, trelloConfig.sourceSecret, trelloConfig.baseUrl)
            .get(
            resultClass = resultClass,
            subPath = "/1/${subPath}",
            parameters = parameters)
}