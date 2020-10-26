package org.birdview.storage.model

data class BVUserSettings(
        val email: String?,
        val passwordHash:String,
        val enabled:Boolean = false
)