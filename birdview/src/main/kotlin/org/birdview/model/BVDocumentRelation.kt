package org.birdview.model

sealed class BVDocumentRelation (
        val ref: RefInfo
)

class BVRelatedRelation (
        ref: RefInfo
): BVDocumentRelation(ref)

class BVHierarchyRelation (
        ref: RefInfo,
        val direction: BVRefDir = BVRefDir.OUT
): BVDocumentRelation(ref)

class BVAlternativeRelation (
        ref: RefInfo
): BVDocumentRelation(ref)