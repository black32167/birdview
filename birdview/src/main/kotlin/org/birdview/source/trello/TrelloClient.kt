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
class TrelloClient(
    private val httpClientFactory: BVHttpSourceClientFactory,
    private val sourceConfigProvider: BVSourceConfigProvider) {
    private val log = LoggerFactory.getLogger(TrelloClient::class.java)
    private val cardsPerPage = 50

    fun getCards(bvUser: String, sourceName: String, query:String, chunkConsumer: (List<TrelloCard>) -> Unit) {
        log.info("Running Trello query '{}'", query)
        var page = 0
        while (searchTrelloCards(
                bvUser = bvUser,
                sourceName = sourceName,
                query = query,
                page = page,
                chunkConsumer = chunkConsumer)) {
            page++
        }
    }

    private fun searchTrelloCards(bvUser: String, sourceName: String, query:String, page: Int, chunkConsumer: (List<TrelloCard>) -> Unit): Boolean {
        log.info("Loading trello issues page {} for query {}", page, query)
        val cards = searchTrelloCards(
            bvUser = bvUser,
            sourceName = sourceName,
            query = query,
            cardsPage = page).cards.toList()
        return if(cards.isEmpty()) {
            false
        } else {
            chunkConsumer.invoke(cards)
            true
        }
    }

    fun getBoards(bvUser: String, sourceName: String, boardIds: List<String>): List<TrelloBoard> =
        boardIds.map { boardId ->
            getTrello(
                bvUser = bvUser,
                sourceName = sourceName,
                resultClass = TrelloBoard::class.java,
                subPath = "boards/${boardId}")
        }

    fun loadLists(bvUser: String, sourceName: String, listsIds: List<String>): List<TrelloList> = listsIds.map { listId ->
        getTrello(
            bvUser = bvUser,
            sourceName = sourceName,
            subPath = "lists/${listId}",
            resultClass = TrelloList::class.java
        )
    }

    private fun searchTrelloCards(bvUser: String, sourceName: String, query: String, cardsPage: Int) =
        getTrello(
            bvUser = bvUser,
            sourceName = sourceName,
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

    private fun <T> getTrello(bvUser: String, sourceName: String, resultClass: Class<T>, subPath: String, parameters: Map<String, Any> = emptyMap()): T {
        val sourceConfig = sourceConfigProvider.getSourceConfig(sourceName = sourceName, bvUser = bvUser)
        return httpClientFactory.createClient(
            bvUser = bvUser,
            sourceName = sourceName,
            url = sourceConfig.baseUrl
        ).get(
                resultClass = resultClass,
                subPath = "/1/${subPath}",
                parameters = parameters
            )
    }
}