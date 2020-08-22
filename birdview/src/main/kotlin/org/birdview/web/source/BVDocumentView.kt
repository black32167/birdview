package org.birdview.web.source

class BVDocumentView (
        val id: String,
        val subDocuments: List<BVDocumentView>,
        val title: String,
        val updated: String?,
        val httpUrl: String,
        val status: String,
        val sourceName: String,
        val key: String
)