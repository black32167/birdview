package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.model.BVUserSourceConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FIRESTORE)
@Repository
class BVFireUserSourceStorage(clienProvider: BVFirebaseClientProvider): BVUserSourceStorage {
    private val userCollectionRef = clienProvider.getClientForCollection("users")

    @Cacheable(BVCacheNames.USER_SOURCE_CACHE)
    override fun getSourceProfile(bvUserName: String, sourceName: String): BVUserSourceConfig =
        getUserSourcesCollectionRef(bvUserName)
            .document(sourceName).get().get()
            .toObject(BVUserSourceConfig::class.java)!!

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun create(bvUserName: String, sourceName: String, sourceUserName: String) {
        update(
            bvUserName = bvUserName,
            sourceName = sourceName,
            userProfileSourceConfig = BVUserSourceConfig(sourceUserName, "" != sourceUserName))
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun update(bvUserName: String, sourceName: String, userProfileSourceConfig: BVUserSourceConfig) {
        getUserSourcesCollectionRef(bvUserName)
            .document(sourceName)
            .set(userProfileSourceConfig)
    }

    override fun listUserSources(bvUserName: String): List<String> =
        getUserSourcesCollectionRef(bvUserName)
            .listDocuments()
            .map { it.id }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun delete(bvUserName: String, sourceName: String) {
        getUserSourcesCollectionRef(bvUserName)
            .document(sourceName)
            .delete()
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun deleteAll(bvUserName: String) {
        getUserSourcesCollectionRef(bvUserName)
            .listDocuments()
            .forEach { it.delete() }
    }

    private fun getUserSourcesCollectionRef(bvUserName: String) =
        userCollectionRef.document(bvUserName).collection("sources")
}