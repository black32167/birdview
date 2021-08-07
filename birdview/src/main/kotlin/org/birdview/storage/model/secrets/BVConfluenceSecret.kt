package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

class BVConfluenceSecret (
        sourceName: String = "confluence",
        val baseUrl: String,
        user: String,
        val token: String
): BVAbstractSourceSecret(SourceType.CONFLUENCE, sourceName, user)