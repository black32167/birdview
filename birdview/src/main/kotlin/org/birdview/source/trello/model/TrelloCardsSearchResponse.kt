package org.birdview.source.trello.model

class TrelloCardsSearchResponse (
        val cards: Array<TrelloCard> = arrayOf()
)

class TrelloCard (
        val id: String,
        val idBoard: String,
        val dateLastActivity: String,
        val name: String,
        val desc: String,
        val url: String,
        val labels: Array<TrelloCardLabel>,
        val idList: String,
        val shortLink: String,
        val idMembers: Array<String>
)

class TrelloCardLabel(
        val id: String,
        val name: String
)

