package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.model.BVSourceConfig
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
    override fun getSource(bvUser: String, sourceName: String): BVSourceConfig? =
        collectionAccessor.getUserSourcesCollection(bvUser)
            .document(sourceName).get().get()
            .toObject(BVSourceConfig::class.java)

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun create(bvUser: String, sourceName: String, sourceUserName: String) {
        update(
            bvUser = bvUser,
            sourceConfig = BVSourceConfig(
                sourceName = sourceName, sourceUserName = sourceUserName, enabled = "" != sourceUserName)
        )
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun update(bvUser: String, sourceConfig: BVSourceConfig) {
        collectionAccessor.getUserSourcesCollection(bvUser)
            .document(sourceConfig.sourceName)
            .set(sourceConfig)
    }

    @Cacheable(cacheNames = [BVCacheNames.USER_SOURCE_CACHE], key = "'sn-'.concat(#bvUser)" )
    override fun listSourceNames(bvUser: String): List<String> =
        collectionAccessor.getUserSourcesCollection(bvUser)
            .listDocuments()
            .map { it.id }

    @Cacheable(cacheNames = [BVCacheNames.USER_SOURCE_CACHE], key = "'sc-'.concat(#bvUser)" )
    override fun listSourceProfiles(bvUser: String): List<BVSourceConfig> {
        return collectionAccessor.getUserSourcesCollection(bvUser).get().get()
            .toObjects(BVSourceConfig::class.java)
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