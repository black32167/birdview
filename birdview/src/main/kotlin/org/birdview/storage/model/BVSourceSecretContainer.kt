package org.birdview.storage.model

import org.birdview.source.SourceType

class BVSourceSecretContainer(
    val sourceType: SourceType,
    val sourceName: String,
    val user: String,
    val secretToken: String
)