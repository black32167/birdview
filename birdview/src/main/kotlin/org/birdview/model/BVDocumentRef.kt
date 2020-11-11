package org.birdview.model

class BVDocumentRef (
        val ref: String,
        val refDirection: BVRefDirection = BVRefDirection.UNSPECIFIED,
        val sourceName: String
)
