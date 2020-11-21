package org.birdview.model

import org.birdview.source.SourceType

class RefInfo (
        val ref: String,
        val sourceType: SourceType = SourceType.UNDEFINED

)