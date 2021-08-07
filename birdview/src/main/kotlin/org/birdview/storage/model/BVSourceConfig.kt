package org.birdview.storage.model

import org.birdview.source.SourceType
import org.birdview.storage.model.secrets.BVAbstractSourceSecret

class BVSourceConfig (
        val sourceName: String = "",
        val sourceUserName: String = "",
        val sourceType: SourceType? = null,
        val enabled: Boolean = false,
        val sourceSecret: BVAbstractSourceSecret? = null
)