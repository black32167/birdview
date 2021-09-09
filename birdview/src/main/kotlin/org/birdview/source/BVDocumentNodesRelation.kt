package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.RelativeHierarchyType
import org.birdview.web.explore.model.BVDocumentViewTreeNode

class BVDocumentsRelation (
    val parent: BVDocument,
    val child: BVDocument
)
class BVDocumentNodesRelation (
        val parent: BVDocumentViewTreeNode,
        val child: BVDocumentViewTreeNode
) {
    companion object {
        fun getHierarchyLevel(sourceType: SourceType): Int = when (sourceType) {
            SourceType.JIRA, SourceType.TRELLO -> 1
            SourceType.SLACK -> 2
            SourceType.GDRIVE, SourceType.CONFLUENCE -> 3
            SourceType.GITHUB -> 4
            SourceType.UNDEFINED -> 100
            SourceType.NONE -> 1000
        }

        fun from(referencedNode: BVDocument, node: BVDocument, hierarchyType: RelativeHierarchyType): BVDocumentsRelation? =
            when (hierarchyType) {
                RelativeHierarchyType.UNSPECIFIED -> {
                    val sourceTypePriorityDiff = signInt(getHierarchyLevel(referencedNode.sourceType) - getHierarchyLevel(node.sourceType))
                    when(sourceTypePriorityDiff) {
                        -1 -> BVDocumentsRelation(referencedNode, node)
                        1 -> BVDocumentsRelation(node, referencedNode)
                        else  -> null
                    }
                }
                RelativeHierarchyType.LINK_TO_PARENT -> BVDocumentsRelation(referencedNode, node)
                RelativeHierarchyType.LINK_TO_CHILD -> BVDocumentsRelation(node, referencedNode)
                else  -> null
            }

        fun from(referencedNode: BVDocumentViewTreeNode, node: BVDocumentViewTreeNode, hierarchyType: RelativeHierarchyType): BVDocumentNodesRelation? =
            when (hierarchyType) {
                RelativeHierarchyType.UNSPECIFIED -> {
                    val sourceTypePriorityDiff = signInt(getHierarchyLevel(referencedNode.sourceType) - getHierarchyLevel(node.sourceType))
//                    val diff = sourceTypePriorityDiff.takeIf { it != 0 }
//                            ?: signLong(millis(referncedDoc.created) - millis(doc.created))
                    when(sourceTypePriorityDiff) {
                        -1 -> BVDocumentNodesRelation(referencedNode, node)
                        1 -> BVDocumentNodesRelation(node, referencedNode)
                        else  -> null
                    }
                }
                RelativeHierarchyType.LINK_TO_PARENT -> BVDocumentNodesRelation(referencedNode, node)
                RelativeHierarchyType.LINK_TO_CHILD -> BVDocumentNodesRelation(node, referencedNode)
                else -> null
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

        fun getHierarchyRelationType(sourceTypeFrom: SourceType, sourceTypeTo: SourceType?): RelativeHierarchyType {
            if (sourceTypeTo == null) {
                return RelativeHierarchyType.LINK_TO_CHILD
            }
            val delta = getHierarchyLevel(sourceTypeFrom) - getHierarchyLevel(sourceTypeTo)
            return if (delta > 0) {
                RelativeHierarchyType.LINK_TO_PARENT
            } else if (delta < 0) {
                RelativeHierarchyType.LINK_TO_CHILD
            } else {
                RelativeHierarchyType.UNSPECIFIED
            }
        }
    }
}
