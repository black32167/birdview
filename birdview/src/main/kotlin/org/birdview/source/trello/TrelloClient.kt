package org.birdview.source.trello

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.source.trello.model.TrelloBoard
import org.birdview.source.trello.model.TrelloCard
import org.birdview.source.trello.model.TrelloCardsSearchResponse
import org.birdview.source.trello.model.TrelloList
import org.birdview.storage.model.secrets.BVTrelloConfig
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class TrelloClient(private val httpClientFactory: BVHttpClientFactory) {
    private val log = LoggerFactory.getLogger(TrelloClient::class.java)
    private val cardsPerPage = 50

    fun getCards(trelloConfig: BVTrelloConfig, query:String, chunkConsumer: (List<TrelloCard>) -> Unit) {
        log.info("Running Trello query '{}'", query)
        var page = 0
        while (searchTrelloCards(trelloConfig, query, page, chunkConsumer)) {
            page++
        }
    }

    private fun searchTrelloCards(trelloConfig: BVTrelloConfig, query:String, page: Int, chunkConsumer: (List<TrelloCard>) -> Unit): Boolean {
        log.info("Loading trello issues page {} for query {}", page, query)
        val cards = searchTrelloCards(trelloConfig, query, page).cards.toList()
        return if(cards.isEmpty()) {
            false
        } else {
            chunkConsumer.invoke(cards)
            true
        }
    }

    fun getBoards(trelloConfig: BVTrelloConfig, boardIds: List<String>): List<TrelloBoard> =
        boardIds.map { boardId ->
            getTrello(
                trelloConfig = trelloConfig,
                resultClass = TrelloBoard::class.java,
                subPath = "boards/${boardId}")
        }

    fun loadLists(trelloConfig: BVTrelloConfig, listsIds: List<String>): List<TrelloList> = listsIds.map { listId ->
        getTrello(
            trelloConfig = trelloConfig,
            subPath = "lists/${listId}",
            resultClass = TrelloList::class.java
        )
    }

    private fun searchTrelloCards(trelloConfig: BVTrelloConfig, query: String, cardsPage: Int) =
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

    private fun <T> getTrello(trelloConfig: BVTrelloConfig, resultClass: Class<T>, subPath: String, parameters: Map<String, Any> = emptyMap()) =
        httpClientFactory.getHttpClient("${trelloConfig.baseUrl}/1").get(
            resultClass = resultClass,
            subPath = subPath,
            parameters = parameters + mapOf(
                "key" to  trelloConfig.key,
                "token" to trelloConfig.token
            )
        )
}