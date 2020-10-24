package org.birdview.web.explore.model

import java.util.*

// !!! Should NOT be data-class
class BVDocumentViewTreeNode (
        val doc: BVDocumentView,
        var subNodes: MutableList<BVDocumentViewTreeNode> = mutableListOf(),
        var lastUpdated: Date?
)