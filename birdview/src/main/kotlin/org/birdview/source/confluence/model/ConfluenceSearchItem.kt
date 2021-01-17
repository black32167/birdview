package org.birdview.source.confluence.model

class ConfluenceSearchItem (
        val title: String,
        val url: String,
        val lastModified: String,
        val excerpt: String?,
        val content: ConfluenceSearchItemContent?
)
