package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.model.secrets.BVAbstractSourceConfig
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FIRESTORE)
@Repository
open class BVFireSourceSecretsStorage(
    clientProvider: BVFirebaseClientProvider,
    open val secretsMapper: FireUserSecretsMapper
) : BVSourceSecretsStorage {
    private val log = LoggerFactory.getLogger(BVFireSourceSecretsStorage::class.java)
    private val secretsCollectionRef = clientProvider.getClientForCollection("storageSecrets")

    @Cacheable(BVCacheNames.SOURCE_SECRET_CACHE_NAME)
    override fun getSecret(sourceName: String): BVAbstractSourceConfig? =
        secretsCollectionRef
            .document(sourceName).get().get()
            ?.let { secretsMapper.extractSecrets(it) }

    @Cacheable(BVCacheNames.SOURCE_SECRET_CACHE_NAME)
    override fun <T : BVAbstractSourceConfig> getSecret(sourceName: String, configClass: Class<T>): T? =
        getSecret(sourceName)
            .let { configClass.cast(it) }

    @Cacheable(BVCacheNames.SOURCE_SECRET_CACHE_NAME, key = "all")
    override fun getSecrets(): List<BVAbstractSourceConfig> =
        secretsCollectionRef.get().get()
            .mapNotNull { secretsMapper.extractSecrets(it) }

    override fun listSourceNames(): List<String> =
        secretsCollectionRef.listDocuments()
            .map { it.id }

    @CacheEvict(BVCacheNames.SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun create(config: BVAbstractSourceConfig) {
        update(config)
    }

    @CacheEvict(BVCacheNames.SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun update(config: BVAbstractSourceConfig) {
        val configContainer = secretsMapper.toContainer(config)
        secretsCollectionRef.document(config.sourceName).set(configContainer).get()
    }

    @CacheEvict(BVCacheNames.SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun delete(sourceName: String) {
        secretsCollectionRef.document(sourceName)
            .delete()
            .get()
    }
}
