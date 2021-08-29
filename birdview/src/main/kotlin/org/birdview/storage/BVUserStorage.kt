package org.birdview.storage

import org.birdview.storage.model.BVUserSettings

interface BVUserStorage {
    class UserStorageException: Exception {
        constructor(message: String) : super(message)
        constructor(message: String, cause: Throwable) : super(message, cause)
    }
    fun listUserNames(): List<String>

    @Throws(UserStorageException::class)
    fun create(userName:String, userSettings: BVUserSettings)

    fun update(userName:String, userSettings: BVUserSettings)

    fun getUserSettings(userName: String): BVUserSettings

    fun updateUserStatus(userName: String, enabled: Boolean)

    fun delete(userName: String)

    fun deleteGroup(bvUser: String, groupName: String)
}