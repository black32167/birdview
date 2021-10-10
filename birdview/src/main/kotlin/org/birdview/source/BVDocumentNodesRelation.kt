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
        val child: BVDocumentViewTreeNode,
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
