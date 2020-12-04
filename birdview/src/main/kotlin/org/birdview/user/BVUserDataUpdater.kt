package org.birdview.user

import org.birdview.web.explore.model.BVUserDocumentCorpusStatus

interface BVUserDataUpdater {
    fun refreshUser(bvUser: String)
    fun waitForUserUpdated(userAlias: String)
    fun getStatusForUser(bvUser: String): BVUserDocumentCorpusStatus?
}