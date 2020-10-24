package org.birdview.web.explore

import org.birdview.model.ReportType
import org.birdview.web.explore.model.BVDocumentViewTreeNode

object HierarchySorter {
    fun sortHierarchy(roots: MutableList<BVDocumentViewTreeNode>, reportType: ReportType) {
        val comparator: Comparator<BVDocumentViewTreeNode> = if (reportType == ReportType.WORKED) {
            compareByDescending<BVDocumentViewTreeNode> { it.lastUpdated }
                    .thenByDescending { it.doc.priority.ordinal }
        } else {
            compareByDescending<BVDocumentViewTreeNode>{ it.doc.priority.ordinal }
                    .thenByDescending { it.lastUpdated }
        }

        sortHierarchy(roots, comparator)
    }

    private fun sortHierarchy(roots: MutableList<BVDocumentViewTreeNode>, comparator: Comparator<BVDocumentViewTreeNode>) {
        roots.sortWith(comparator)
        roots.forEach { node->
            sortHierarchy(node.subNodes, comparator)
        }
    }
}