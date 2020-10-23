package org.birdview.web.explore

import org.birdview.web.explore.model.BVDocumentViewTreeNode

object HierarchyOptimizer {
    fun optimizeHierarchy(node: BVDocumentViewTreeNode) {
        node.subNodes = optimizesChildren(node.subNodes)
        node.subNodes.forEach { children ->
            optimizeHierarchy(children)
        }
    }

    private fun optimizesChildren(nodes: List<BVDocumentViewTreeNode>): MutableList<BVDocumentViewTreeNode> =
            nodes.flatMap { node ->
                if (node.subNodes.size < 3) {
                    node.subNodes
                } else {
                    listOf(node)
                }
            }.toMutableList()
}
