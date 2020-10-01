package org.birdview.config.user

interface BVUserProfileStorage {
    fun getUserName(userAlias: String?, sourceName: String): String

    fun listUsers(): List<String>
}
