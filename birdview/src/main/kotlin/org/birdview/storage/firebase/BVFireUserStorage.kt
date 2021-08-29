package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentReference
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
    open val fireStore: BVFireStoreAccessor,
    private val userSourceStorage: BVUserSourceConfigStorage
): BVUserStorage {
    @Cacheable(BVCacheNames.USER_NAMES_CACHE)
    override fun listUserNames(): List<String> {
        return fireStore.getUserCollection()
            .listDocuments()
            .map { doc -> doc.id }
    }

    @CacheEvict(BVCacheNames.USER_NAMES_CACHE, allEntries = true)
    override fun create(userName: String, userSettings: BVUserSettings) {
        update(userName, userSettings)
    }

    @CacheEvict(BVCacheNames.USER_SETTINGS_CACHE, allEntries = true)
    override fun update(userName: String, userSettings: BVUserSettings) {
        fireStore.getUserCollection()
            .document(userName).set(userSettings).get()
    }

    @Cacheable(BVCacheNames.USER_SETTINGS_CACHE)
    override fun getUserSettings(userName: String): BVUserSettings =
        fireStore.getUserCollection()
            .document(userName).get().get()
            .let { DocumentObjectMapper.toObjectCatching(it, BVUserSettings::class)!! }

    @CacheEvict(BVCacheNames.USER_SETTINGS_CACHE, allEntries = true)
    override fun updateUserStatus(userName: String, enabled: Boolean) {
        fireStore.getUserCollection()
            .document(userName)
            .update(BVUserSettings::enabled.name, true)
            .get()
    }

    @CacheEvict(cacheNames = [BVCacheNames.USER_SETTINGS_CACHE, BVCacheNames.USER_NAMES_CACHE], allEntries = true)
    override fun delete(userName: String) {
        userSourceStorage.deleteAll(userName)
        fireStore.getUserCollection()
            .document(userName)
            .delete()
            .get()
    }

    @CacheEvict(BVCacheNames.USER_SETTINGS_CACHE, allEntries = true)
    override fun deleteGroup(bvUser: String, groupName: String) {
        fireStore.getUserCollection()
            .document(bvUser)
            .also { docRef:DocumentReference ->
                fireStore.db().runTransaction { transaction ->
                    val existingDoc = DocumentObjectMapper.toObjectCatching(
                        transaction.get(docRef).get(), BVUserSettings::class)!!
                    val updatedDoc = existingDoc.copy(workGroups = existingDoc.workGroups - groupName)
                    transaction.set(docRef, updatedDoc)
                }.get()
            }
    }
}
