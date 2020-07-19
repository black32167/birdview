package org.birdview.source.trello

import org.birdview.config.BVTrelloConfig
import org.birdview.source.BVTaskListsDefaults
import org.birdview.source.trello.model.TrelloBoard
import org.birdview.source.trello.model.TrelloCard
import org.birdview.source.trello.model.TrelloCardsSearchResponse
import org.birdview.source.trello.model.TrelloList
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory

class TrelloClient(private val trelloConfig: BVTrelloConfig,
                   private val sourceConfig: BVTaskListsDefaults) {
    private val log = LoggerFactory.getLogger(TrelloClient::class.java)
    fun getCards(query:String): List<TrelloCard> {
        log.info("Running Trello query '{}'", query)
        val trelloCardsResponse = getTarget().path("search")
                .queryParam("query", query)
                .queryParam("partial", true)
                .queryParam("cards_limit", sourceConfig.getMaxResult())
                .request()
                .get()

        if(trelloCardsResponse.status != 200) {
            throw RuntimeException("Error reading Trello cards: ${trelloCardsResponse.readEntity(String::class.java)}")
        }
        return trelloCardsResponse.readEntity(TrelloCardsSearchResponse::class.java).cards.toList()
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

    private fun getTarget() = WebTargetFactory(trelloConfig.baseUrl)
            .getTarget("/1")
            .queryParam("key", trelloConfig.key)
            .queryParam("token", trelloConfig.token)

}