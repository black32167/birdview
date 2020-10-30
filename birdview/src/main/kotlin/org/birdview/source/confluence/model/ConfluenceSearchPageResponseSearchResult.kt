package org.birdview.source.confluence.model

class ConfluenceSearchPageResponseSearchResult (
        val start: Int,
        val limit: Int,
        val size: Int,
        val totalSize: Int,
        val results: List<ConfluenceSearchItem>
)