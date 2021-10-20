package org.birdview.storage.firebase

import org.birdview.source.SourceType

class BVFirePersistingDocument (
    val id: String,
    val content:String, // Serialized BVDocument
    val sourceName: String,
    val sourceType: SourceType,
    val updated: Long,
    val indexed: Long,
    val bvUser: String,
    val externalIds: List<String>,
    val externalRefs: List<String>
)