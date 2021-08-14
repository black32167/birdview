package org.birdview.storage.model.source.config

import org.birdview.source.SourceType

abstract class BVSourceConfig(
    val sourceName: String,
    val sourceType: SourceType,
    val baseUrl: String
)