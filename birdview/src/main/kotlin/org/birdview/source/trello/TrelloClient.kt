package org.birdview.source.trello

import org.birdview.source.BVTaskListsDefaults
import org.birdview.source.ItemsPage
import org.birdview.source.trello.model.TrelloBoard
import org.birdview.source.trello.model.TrelloCard
import org.birdview.source.trello.model.TrelloCardsSearchResponse
import org.birdview.source.trello.model.TrelloList
import org.birdview.storage.BVTrelloConfig
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response

class TrelloClient(private val trelloConfig: BVTrelloConfig,
                   private val sourceConfig: BVTaskListsDefaults) {
    private val log = LoggerFactory.getLogger(TrelloClient::class.java)
    private val cardsPerPage = 50

    fun getCards(query:String, chunkConsumer: (List<TrelloCard>) -> Unit) {
        log.info("Running Trello query '{}'", query)
        var page = 0
        while (searchTrelloCards(query, page, chunkConsumer)) {
            page++
        }
    }

    private fun searchTrelloCards(query:String, page: Int, chunkConsumer: (List<TrelloCard>) -> Unit): Boolean {
        log.info("Loading trello issues page {} for query {}", page, query)
        var cards = searchTrelloCards(query, page)
                .let { mapCardsList(it) }
        return if(cards.isEmpty()) {
            false
        } else {
            chunkConsumer.invoke(cards)
            true
        }
    }

    fun getBoards(boardIds: List<String>): List<TrelloBoard> =
        boardIds.map { boardId ->
            getTarget()
                    .path("boards")
                    .path(boardId)
                    .request()
                    .get()
        }
        .map { response->
            if(response.status != 200) {
                throw RuntimeException("Error reading Trello board: ${response.readEntity(String::class.java)}")
            }
//            println(response.readEntity(String::class.java))
            response.readEntity(TrelloBoard::class.java)
        }

    fun loadLists(listsIds: List<String>): List<TrelloList> = listsIds.map { listId ->
        getTarget()
                .path("lists")
                .path(listId)
                .request()
                .get()
        }
        .map { response->
            if(response.status != 200) {
                throw RuntimeException("Error reading Trello list: ${response.readEntity(String::class.java)}")
            }
            response.readEntity(TrelloList::class.java)
        }

    private fun searchTrelloCards(query: String, cardsPage: Int) = getTarget().path("search")
            .queryParam("query", query)
            .queryParam("partial", true)
        //    .queryParam("card_fields", "idMembers,dateLastActivity,dateLastView,desc,descData,idOrganization,invitations,invited,labelNames,memberships,name,pinned,powerUps,prefs,shortLink,shortUrl,starred,subscribed,url")
            .queryParam("cards_limit", cardsPerPage)
            .queryParam("cards_page", cardsPage)
            .request()
            .get()

    private fun mapCardsPage(response: Response, page:Int): ItemsPage<TrelloCard, Int> =
            response
                    .also (ResponseValidationUtils::validate)
                    .let { resp ->
                        val cardsResponse = resp.readEntity(TrelloCardsSearchResponse::class.java)
                        ItemsPage (
                                cardsResponse.cards.toList(),
                                page + 1)
                    }

    private fun mapCardsList(response: Response): List<TrelloCard> =
            response
                    .also (ResponseValidationUtils::validate)
                    .let { resp -> resp.readEntity(TrelloCardsSearchResponse::class.java) }
                    .cards
                    .toList()

    private fun getTarget() = WebTargetFactory(trelloConfig.baseUrl)
            .getTarget("/1")
            .queryParam("key", trelloConfig.key)
            .queryParam("token", trelloConfig.token)

}