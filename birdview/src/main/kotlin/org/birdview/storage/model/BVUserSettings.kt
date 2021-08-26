package org.birdview.storage.model

import java.time.ZoneId

data class BVUserSettings(
        val email: String? = null,
        val passwordHash:String = "",
        val enabled:Boolean = false,
        val zoneId: String = ZoneId.of("UTC").id
)