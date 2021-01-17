package org.birdview.source.confluence.model

class ConfluenceSearchItemContent (
        val id: String,
        val type: String,
        val title: String,
        val version: ConfluenceVersion,
        val history: ConfluenceHistory,
        val _expandable: ConfluenceContentExpandable,
        val _links: ConfluenceContentLinks,
)

class ConfluenceContentExpandable (
        val container: String,
        val body: String?
 )

class ConfluenceContentLinks (
        val webui: String
        )