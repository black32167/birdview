package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FIRESTORE)
@Repository
open class BVFireUserSourceConfigStorage(
    open val collectionAccessor: BVFireStoreAccessor
): BVUserSourceConfigStorage {

    @Cacheable(BVCacheNames.USER_SOURCE_CACHE)
    override fun getSource(bvUser: String, sourceName: String): BVUserSourceConfig? =
        collectionAccessor.getUserSourcesCollection(bvUser)
            .document(sourceName).get().get()
            .takeIf { it.exists() }
            ?.let { doc -> DocumentObjectMapper.toObjectCatching(doc, BVUserSourceConfig::class)  }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun create(bvUser: String, sourceConfig: BVUserSourceConfig) {
        update(
            bvUser = bvUser,
            sourceConfig = sourceConfig
        )
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun update(bvUser: String, sourceConfig: BVUserSourceConfig) {
        collectionAccessor.getUserSourcesCollection(bvUser)
            .document(sourceConfig.sourceName)
            .set(sourceConfig)
            .get()
    }

    @Cacheable(cacheNames = [BVCacheNames.USER_SOURCE_CACHE], key = "'sn-'.concat(#bvUser)" )
    override fun listSourceNames(bvUser: String): List<String> =
        collectionAccessor.getUserSourcesCollection(bvUser)
            .listDocuments()
            .map { it.id }

    @Cacheable(cacheNames = [BVCacheNames.USER_SOURCE_CACHE], key = "'sc-'.concat(#bvUser)" )
    override fun listSources(bvUser: String): List<BVUserSourceConfig> {
        return collectionAccessor.getUserSourcesCollection(bvUser).get().get()
            .mapNotNull { doc -> DocumentObjectMapper.toObjectCatching(doc, BVUserSourceConfig::class)  }
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun delete(bvUser: String, sourceName: String) {
        collectionAccessor.getUserSourcesCollection(bvUser)
            .document(sourceName)
            .delete()
            .get()
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun deleteAll(bvUser: String) {
        collectionAccessor.getUserSourcesCollection(bvUser)
            .listDocuments()
            .map { it.delete() }
            .forEach { it.get() }
    }
}