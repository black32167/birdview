package org.birdview.user

import org.birdview.security.UserContext
import org.birdview.storage.BVUserStorage
import javax.inject.Named

@Named("loggedUserSettingsProvider")
open class BVLoggedUserSettingsProvider(private val userStorage: BVUserStorage) {
    open fun getTimezoneId(): String =
        userStorage.getUserSettings(UserContext.getUserName()).zoneId
}