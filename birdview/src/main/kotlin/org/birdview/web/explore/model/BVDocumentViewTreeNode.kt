package org.birdview.web.explore.model

import org.birdview.source.SourceType
import java.util.*

// !!! Should NOT be data-class
class BVDocumentViewTreeNode (
        val doc: BVDocumentView,
        val sourceType: SourceType,
        var lastUpdated: Date?
) {
    var subNodes: MutableList<BVDocumentViewTreeNode> = mutableListOf()
    val alternativeNodes: MutableList<BVDocumentViewTreeNode> = mutableListOf()
    private var referencesCount = 0

    fun addSubNode(node: BVDocumentViewTreeNode) {
        subNodes.add(node)
        node.referencesCount++
    }

    fun addAlternative(child: BVDocumentViewTreeNode): Boolean {
        if (child.alternativeNodes.contains(this)) {
            return false;
        }
        alternativeNodes += child
        alternativeNodes.addAll(child.alternativeNodes)
        subNodes.addAll(child.subNodes)
        return true;
    }

    fun isRoot() = referencesCount == 0
}