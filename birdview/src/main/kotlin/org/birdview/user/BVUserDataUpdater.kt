package org.birdview.user

import org.birdview.web.explore.model.BVUserLogEntry

interface BVUserDataUpdater {
    fun requestUserRefresh(bvUser: String)
    fun waitForUserUpdated(bvUser: String)
}