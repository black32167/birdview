package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FIRESTORE)
@Repository
class BVFireUserStorage(
    clientProvider: BVFirebaseClientProvider,
    private val userSourceStorage: BVUserSourceStorage
): BVUserStorage {
    private val userCollectionRef = clientProvider.getClientForCollection("users")

    @Cacheable(BVCacheNames.USER_NAMES_CACHE)
    override fun listUserNames(): List<String> {
        return userCollectionRef.listDocuments()
            .map { doc -> doc.id }
    }

    @CacheEvict(BVCacheNames.USER_NAMES_CACHE, allEntries = true)
    override fun create(userName: String, userSettings: BVUserSettings) {
        update(userName, userSettings)
    }

    @CacheEvict(BVCacheNames.USER_SETTINGS_CACHE, allEntries = true)
    override fun update(userName: String, userSettings: BVUserSettings) {
        userCollectionRef.document(userName).set(userSettings)
    }

    @Cacheable(BVCacheNames.USER_SETTINGS_CACHE)
    override fun getUserSettings(userName: String): BVUserSettings =
        userCollectionRef
            .document(userName).get().get()
            .toObject(BVUserSettings::class.java)!!

    @CacheEvict(BVCacheNames.USER_SETTINGS_CACHE, allEntries = true)
    override fun updateUserStatus(userName: String, enabled: Boolean) {
        userCollectionRef
            .document(userName)
            .update(BVUserSettings::enabled.name, true)
    }

    @CacheEvict(cacheNames = [BVCacheNames.USER_SETTINGS_CACHE, BVCacheNames.USER_NAMES_CACHE], allEntries = true)
    override fun delete(userName: String) {
        userSourceStorage.deleteAll(userName)
        userCollectionRef
            .document(userName)
            .delete()
    }
}
