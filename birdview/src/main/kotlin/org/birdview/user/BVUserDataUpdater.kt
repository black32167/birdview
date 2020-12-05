package org.birdview.user

import org.birdview.web.explore.model.BVUserLogEntry

interface BVUserDataUpdater {
    fun refreshUser(bvUser: String)
    fun waitForUserUpdated(userAlias: String)
}