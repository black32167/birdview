package org.birdview.model

import org.birdview.analysis.BVDocumentId

open class BVDocumentRef (
        val docId: BVDocumentId,
        val hierarchyPosition: RelativeHierarchyPosition = RelativeHierarchyPosition.UNSPECIFIED
)

enum class RelativeHierarchyPosition {
    UNSPECIFIED, LINK_TO_PARENT, LINK_TO_CHILD
}
