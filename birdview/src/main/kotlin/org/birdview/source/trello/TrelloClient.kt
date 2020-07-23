package org.birdview.source.trello

import org.birdview.config.BVTrelloConfig
import org.birdview.source.BVTaskListsDefaults
import org.birdview.source.ItemsIterable
import org.birdview.source.ItemsPage
import org.birdview.source.trello.model.TrelloBoard
import org.birdview.source.trello.model.TrelloCard
import org.birdview.source.trello.model.TrelloCardsSearchResponse
import org.birdview.source.trello.model.TrelloList
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response

class TrelloClient(private val trelloConfig: BVTrelloConfig,
                   private val sourceConfig: BVTaskListsDefaults) {
    private val log = LoggerFactory.getLogger(TrelloClient::class.java)
    private val cardsPerPage = 50

    fun getCards(query:String): Iterable<TrelloCard> {
        log.info("Running Trello query '{}'", query)
        return searchTrelloCards(query, 0)
                .let { resp ->
                    ItemsIterable(mapCardsPage(resp, 0)) { page ->
                        log.info("Loading trello issues next page: {}", page)
                        searchTrelloCards(query, page)
                                ?.let { mapCardsPage(it, page) }
                    }
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

    private fun getTarget() = WebTargetFactory(trelloConfig.baseUrl)
            .getTarget("/1")
            .queryParam("key", trelloConfig.key)
            .queryParam("token", trelloConfig.token)

}