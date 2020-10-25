package org.birdview.storage

import org.birdview.storage.model.BVUserSettings

interface BVUserStorage {
    interface UserChangedListener {
        fun onUserCreated(bvUser: String)
        fun onUserDeleted(bvUser: String)
    }
    fun listUserNames(): List<String>

    fun create(userName:String, userSettings: BVUserSettings)

    fun update(userName:String, userSettings: BVUserSettings)

    fun getUserSettings(userName: String): BVUserSettings

    fun updateUserStatus(userName: String, enabled: Boolean)

    fun addUserCreatedListener(userChangedListener: UserChangedListener)
}