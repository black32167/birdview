package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FIRESTORE)
@Repository
open class BVFireUserStorage(
    open val collectionAccessor: BVFireCollectionAccessor,
    private val userSourceStorage: BVUserSourceConfigStorage
): BVUserStorage {
    @Cacheable(BVCacheNames.USER_NAMES_CACHE)
    override fun listUserNames(): List<String> {
        return collectionAccessor.getUserCollection()
            .listDocuments()
            .map { doc -> doc.id }
    }

    @CacheEvict(BVCacheNames.USER_NAMES_CACHE, allEntries = true)
    override fun create(userName: String, userSettings: BVUserSettings) {
        update(userName, userSettings)
    }

    @CacheEvict(BVCacheNames.USER_SETTINGS_CACHE, allEntries = true)
    override fun update(userName: String, userSettings: BVUserSettings) {
        collectionAccessor.getUserCollection()
            .document(userName).set(userSettings).get()
    }

    @Cacheable(BVCacheNames.USER_SETTINGS_CACHE)
    override fun getUserSettings(userName: String): BVUserSettings =
        collectionAccessor.getUserCollection()
            .document(userName).get().get()
            .toObject(BVUserSettings::class.java)!!

    @CacheEvict(BVCacheNames.USER_SETTINGS_CACHE, allEntries = true)
    override fun updateUserStatus(userName: String, enabled: Boolean) {
        collectionAccessor.getUserCollection()
            .document(userName)
            .update(BVUserSettings::enabled.name, true)
            .get()
    }

    @CacheEvict(cacheNames = [BVCacheNames.USER_SETTINGS_CACHE, BVCacheNames.USER_NAMES_CACHE], allEntries = true)
    override fun delete(userName: String) {
        userSourceStorage.deleteAll(userName)
        collectionAccessor.getUserCollection()
            .document(userName)
            .delete()
            .get()
    }
}
