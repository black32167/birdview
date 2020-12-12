package org.birdview.web.explore

import org.birdview.web.explore.model.BVDocumentViewTreeNode

object HierarchyOptimizer {
    fun optimizeHierarchy(node: BVDocumentViewTreeNode) {
        node.subNodes.forEach { children ->
            optimizeHierarchy(children)
        }
        node.subNodes = optimizeChildren(node.subNodes)

    }

    private fun optimizeChildren(nodes: Set<BVDocumentViewTreeNode>): MutableSet<BVDocumentViewTreeNode> =
            nodes.flatMap { node ->
                if (node.subNodes.size in 1..2) {
                    node.subNodes
                } else {
                    listOf(node)
                }
            }.toMutableSet()
}
