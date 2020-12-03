package org.birdview.web.explore.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.birdview.source.SourceType
import java.util.*

// !!! Should NOT be data-class
class BVDocumentViewTreeNode (
        val doc: BVDocumentView,
        val sourceType: SourceType,
        var lastUpdated: Date?,
        val subNodesComparator: Comparator<BVDocumentViewTreeNode>
) {
    var subNodes: MutableSet<BVDocumentViewTreeNode> = mutableSetOf()

    @JsonIgnore
    var referringNodes: MutableSet<BVDocumentViewTreeNode> = mutableSetOf()
    val alternativeDocs: MutableSet<BVDocumentView> = mutableSetOf()
    val internalId: String
        get() = doc.internalId

    fun addSubNode(node: BVDocumentViewTreeNode) {
        subNodes.add(node)
        node.referringNodes.add(this)
    }

    fun addAlternative(otherNode: BVDocumentViewTreeNode) {
        alternativeDocs += otherNode.doc
        alternativeDocs.addAll(otherNode.alternativeDocs.filter { it.internalId != this.doc.internalId })

        subNodes.remove(otherNode)
        referringNodes.remove(otherNode)

        otherNode.referringNodes.filter { it != this }.forEach { referringNode->
            referringNode.subNodes.remove(otherNode)
            referringNode.subNodes.add(this)
        }
        otherNode.subNodes.filter { it != this }.forEach { subNode->
            subNode.referringNodes.remove(otherNode)
            subNode.referringNodes.add(this)
        }

        subNodes.addAll(otherNode.subNodes.filter { it != this })
        referringNodes.addAll(otherNode.referringNodes.filter { it != this })
    }

    fun isRoot() = referringNodes.isEmpty()
}