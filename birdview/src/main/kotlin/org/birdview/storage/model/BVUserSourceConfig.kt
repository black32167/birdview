package org.birdview.storage.model

import org.birdview.storage.BVAbstractSourceConfig

class BVUserSourceConfig (
        val sourceUserName: String = "",

        val enabled: Boolean = false,
        val sourceConfig: BVAbstractSourceConfig? = null // sourceConfig.sourceName should include $bvUser as suffix
)