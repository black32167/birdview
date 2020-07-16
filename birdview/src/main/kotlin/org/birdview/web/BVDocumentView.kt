package org.birdview.web

import java.util.*

class BVDocumentView (
        val id: String,
        val subDocuments: List<BVDocumentView>,
        val title: String,
        val updated: Date?,
        val httpUrl: String,
        val status: String,
        val sourceName: String,
        val key: String
)