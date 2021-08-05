package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceSecretsStorage
import org.birdview.storage.model.secrets.BVAbstractSourceConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FIRESTORE)
@Repository
open class BVFireUserSourceSecretsStorage(
    open val collectionAccessor: BVFireCollectionAccessor,
    open val secretsMapper: FireUserSecretsMapper
): BVUserSourceSecretsStorage {

    @Cacheable(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME)
    override fun getSecret(bvUser:String, sourceName: String): BVAbstractSourceConfig? =
        collectionAccessor.getUserSourceCredentialsCollection(bvUser)
            .document(sourceName).get().get()
            ?.let { secretsMapper.extractSecrets(it) }

    @Cacheable(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME, key = "all")
    override fun getSecrets(bvUser:String): List<BVAbstractSourceConfig> =
        collectionAccessor.getUserSourceCredentialsCollection(bvUser).get().get()
            .mapNotNull { secretsMapper.extractSecrets(it) }

    @CacheEvict(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun create(bvUser:String, config: BVAbstractSourceConfig) {
        update(bvUser, config)
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun update(bvUser:String, config: BVAbstractSourceConfig) {
        val configContainer = secretsMapper.toContainer(config)
        collectionAccessor.getUserSourceCredentialsCollection(bvUser)
            .document(config.sourceName).set(configContainer).get()
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun delete(bvUser:String, sourceName: String) {
        collectionAccessor.getUserSourceCredentialsCollection(bvUser).document(sourceName)
            .delete()
            .get()
    }
}