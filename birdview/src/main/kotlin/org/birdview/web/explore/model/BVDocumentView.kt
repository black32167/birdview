package org.birdview.web.explore.model

data class BVDocumentView (
        val id: String,
        val ids: List<String>,
        val title: String,
        val updated: String?,
        val httpUrl: String,
        val status: String,
        val sourceName: String,
        val key: String,
        val lastUpdater: String?
)