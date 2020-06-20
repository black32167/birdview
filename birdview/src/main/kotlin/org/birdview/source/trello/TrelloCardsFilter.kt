package org.birdview.source.trello

import java.time.ZonedDateTime

class TrelloCardsFilter(
        val since: ZonedDateTime,
        val user:String?,
        val listName: String?
)