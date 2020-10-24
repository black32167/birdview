package org.birdview.web.explore.model

import java.util.*

data class BVDocumentViewTreeNode (
        val doc: BVDocumentView,
        var subNodes: MutableList<BVDocumentViewTreeNode> = mutableListOf(),
        var lastUpdated: Date?
)