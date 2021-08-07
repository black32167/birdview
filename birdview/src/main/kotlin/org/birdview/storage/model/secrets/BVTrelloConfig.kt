package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

class BVTrelloConfig (
        sourceName: String = "trello",
        user: String,
        val key: String,
        val token: String
): BVAbstractSourceConfig(SourceType.TRELLO, sourceName, user) {
    val baseUrl = "https://api.trello.com"
}