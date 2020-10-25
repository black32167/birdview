package org.birdview.user

interface BVUserDataUpdater {
    fun refreshUser(bvUser: String)
    fun waitForUserUpdated(userAlias: String)
}