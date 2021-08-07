package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

class BVConfluenceConfig (
        sourceName: String = "confluence",
        val baseUrl: String,
        user: String,
        val token: String
): BVAbstractSourceConfig(SourceType.CONFLUENCE, sourceName, user)