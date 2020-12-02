package org.birdview.web.explore.model

import org.birdview.analysis.Priority

data class BVDocumentView (
        val internalId: String,
        val ids: List<String>,
        val title: String,
        val updated: String?,
        val httpUrl: String,
        val status: String,
        val sourceName: String,
        val key: String,
        val lastUpdater: String?,
        val priority: Priority
)