package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.RelativeHierarchyPosition
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import java.util.*

class BVDocumentsRelation (
        val parent: BVDocumentViewTreeNode,
        val child: BVDocumentViewTreeNode
) {
    companion object {
        fun getPriority(sourceType: SourceType): Int = when (sourceType) {
            SourceType.JIRA, SourceType.TRELLO -> 1
            SourceType.SLACK -> 2
            SourceType.GDRIVE, SourceType.CONFLUENCE -> 3
            SourceType.GITHUB -> 4
            SourceType.UNDEFINED -> 100
        }

        fun from(referencedNode: BVDocumentViewTreeNode, node: BVDocumentViewTreeNode, hierarchyPosition: RelativeHierarchyPosition): BVDocumentsRelation? =
            when (hierarchyPosition) {
                RelativeHierarchyPosition.UNSPECIFIED -> {
                    val sourceTypePriorityDiff = signInt(getPriority(referencedNode.sourceType) - getPriority(node.sourceType))
//                    val diff = sourceTypePriorityDiff.takeIf { it != 0 }
//                            ?: signLong(millis(referncedDoc.created) - millis(doc.created))
                    when(sourceTypePriorityDiff) {
                        -1 -> BVDocumentsRelation(referencedNode, node)
                        1 -> BVDocumentsRelation(node, referencedNode)
                        else  -> null
                    }
                }
                RelativeHierarchyPosition.LINK_TO_PARENT -> BVDocumentsRelation(referencedNode, node)
                RelativeHierarchyPosition.LINK_TO_CHILD -> BVDocumentsRelation(node, referencedNode)
            }

        private fun signInt(x: Int): Int =
                signLong(x.toLong())

        private fun signLong(x: Long): Int =
                if (x < 0) {
                    -1
                } else if (x > 0) {
                    1
                } else {
                    0
                }

        private fun millis(created: Date?): Long =
                created?.time ?: 0
    }
}
