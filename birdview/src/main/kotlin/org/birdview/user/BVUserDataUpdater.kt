package org.birdview.user

interface BVUserDataUpdater {
    fun requestUserRefresh(vararg bvUsers: String)
    fun waitForUserUpdated(bvUser: String)
}