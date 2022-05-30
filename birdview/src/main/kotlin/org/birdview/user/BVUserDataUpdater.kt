package org.birdview.user

interface BVUserDataUpdater {
    fun requestUserRefresh(vararg bvUsers: String)
}