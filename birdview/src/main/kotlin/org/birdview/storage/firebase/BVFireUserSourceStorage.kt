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
open class BVFireUserSourceStorage(
    open val collectionAccessor: BVFireCollectionAccessor
): BVUserSourceStorage {
    @Cacheable(BVCacheNames.USER_SOURCE_CACHE)
    override fun getSourceProfile(bvUser: String, sourceName: String): BVUserSourceConfig =
        collectionAccessor.getUserSourcesCollection(bvUser)
            .document(sourceName).get().get()
            .toObject(BVUserSourceConfig::class.java)!!

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun create(bvUser: String, sourceName: String, sourceUserName: String) {
        update(
            bvUser = bvUser,
            userProfileSourceConfig = BVUserSourceConfig(
                sourceName = sourceName, sourceUserName = sourceUserName, enabled = "" != sourceUserName)
        )
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun update(bvUser: String, userProfileSourceConfig: BVUserSourceConfig) {
        collectionAccessor.getUserSourcesCollection(bvUser)
            .document(userProfileSourceConfig.sourceName)
            .set(userProfileSourceConfig)
    }

    @Cacheable(cacheNames = [BVCacheNames.USER_SOURCE_CACHE], key = "'sn-'.concat(#bvUser)" )
    override fun listUserSources(bvUser: String): List<String> =
        collectionAccessor.getUserSourcesCollection(bvUser)
            .listDocuments()
            .map { it.id }

    @Cacheable(cacheNames = [BVCacheNames.USER_SOURCE_CACHE], key = "'sc-'.concat(#bvUser)" )
    override fun listUserSourceProfiles(bvUser: String): List<BVUserSourceConfig> {
        return collectionAccessor.getUserSourcesCollection(bvUser).get().get()
            .toObjects(BVUserSourceConfig::class.java)
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun delete(bvUser: String, sourceName: String) {
        collectionAccessor.getUserSourcesCollection(bvUser)
            .document(sourceName)
            .delete()
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun deleteAll(bvUser: String) {
        collectionAccessor.getUserSourcesCollection(bvUser)
            .listDocuments()
            .forEach { it.delete() }
    }
}