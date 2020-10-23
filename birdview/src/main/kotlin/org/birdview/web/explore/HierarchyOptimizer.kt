package org.birdview.web.explore

import org.birdview.web.explore.model.BVDocumentViewTreeNode

object HierarchyOptimizer {
    fun optimizeHierarchy(node: BVDocumentViewTreeNode) {
        node.subNodes = optimizeChildren(node.subNodes)
        node.subNodes.forEach { children ->
            optimizeHierarchy(children)
        }
    }

    private fun optimizeChildren(nodes: List<BVDocumentViewTreeNode>): MutableList<BVDocumentViewTreeNode> =
            nodes.flatMap { node ->
                if (node.subNodes.size in 1..2) {
                    node.subNodes
                } else {
                    listOf(node)
                }
            }.toMutableList()
}
