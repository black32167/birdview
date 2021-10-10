package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.RelativeHierarchyType
import org.birdview.source.BVDocumentNodesRelation.Companion.getHierarchyLevel
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import javax.inject.Named

@Named
class BVDocumentRelationFactory (
    val userSourceStorage: BVUserSourceConfigStorage
) {

    fun from(bvUser:String, referencedNode: BVDocument, node: BVDocument, hierarchyType: RelativeHierarchyType): BVDocumentsRelation? =
        when (hierarchyType) {
            RelativeHierarchyType.UNSPECIFIED -> {
                val referenceNodeSourceType = userSourceStorage.getSource(bvUser = bvUser, sourceName = referencedNode.sourceName)?.sourceType ?: SourceType.UNDEFINED
                val nodeSourceType = userSourceStorage.getSource(bvUser = bvUser, sourceName = node.sourceName)?.sourceType ?: SourceType.UNDEFINED
                val sourceTypePriorityDiff = signInt(getHierarchyLevel(referenceNodeSourceType) - getHierarchyLevel(nodeSourceType))
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

}