package org.birdview.storage.model

data class BVUserSettings(
        val passwordHash:String,
        val enabled:Boolean = false
)