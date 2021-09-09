package org.birdview.model

import org.birdview.analysis.BVDocumentId

open class BVDocumentRef (
        val docId: BVDocumentId,
        val hierarchyType: RelativeHierarchyType = RelativeHierarchyType.UNSPECIFIED
)

enum class RelativeHierarchyType {
    UNSPECIFIED, LINK_TO_PARENT, LINK_TO_CHILD, LINK_TO_DEPENDENT, LINK_TO_BLOCKER
}
