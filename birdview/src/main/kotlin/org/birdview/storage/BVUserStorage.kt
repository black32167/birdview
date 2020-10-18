package org.birdview.storage

import org.birdview.storage.model.BVUserSettings

interface BVUserStorage {
    fun listUserNames(): List<String>

    fun create(userName:String, userSettings: BVUserSettings)

    fun update(userName:String, userSettings: BVUserSettings)

    fun getUserSettings(userName: String): BVUserSettings
}