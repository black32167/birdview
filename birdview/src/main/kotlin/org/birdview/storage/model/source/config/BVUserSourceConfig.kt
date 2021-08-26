package org.birdview.storage.model.source.config

import org.birdview.source.SourceType

class BVUserSourceConfig(
    sourceName: String,
    sourceType: SourceType,
    baseUrl: String,
    val sourceUserName: String,
    val enabled: Boolean = false,
    val serializedSourceSecret: String, //BVSourceSecret
): BVSourceConfig(sourceName, sourceType, baseUrl)
