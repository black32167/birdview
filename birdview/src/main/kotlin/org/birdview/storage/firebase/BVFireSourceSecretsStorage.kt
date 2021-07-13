package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.model.BVSourceSecretContainer
import org.birdview.utils.JsonDeserializer
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FIRESTORE)
@Repository
open class BVFireSourceSecretsStorage(
    clientProvider: BVFirebaseClientProvider,
    open val jsonDeserializer: JsonDeserializer
) : BVSourceSecretsStorage {
    private val log = LoggerFactory.getLogger(BVFireSourceSecretsStorage::class.java)
    private val secretsCollectionRef = clientProvider.getClientForCollection("storageSecrets")

    @Cacheable(BVCacheNames.SOURCE_SECRET_CACHE_NAME)
    override fun getConfigByName(sourceName: String): BVAbstractSourceConfig? =
        secretsCollectionRef
            .document(sourceName).get().get()
            ?.let { it.getString(BVSourceSecretContainer::secretToken.name)!! }
            ?.let { deserialize(it) }

    @Cacheable(BVCacheNames.SOURCE_SECRET_CACHE_NAME)
    override fun <T : BVAbstractSourceConfig> getConfigByName(sourceName: String, configClass: Class<T>): T? =
        getConfigByName(sourceName)
            .let { configClass.cast(it) }

    override fun listSourceNames(): List<String> =
        secretsCollectionRef.listDocuments()
            .map { it.id }

    @CacheEvict(BVCacheNames.SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun create(config: BVAbstractSourceConfig) {
        update(config)
    }

    @CacheEvict(BVCacheNames.SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun update(config: BVAbstractSourceConfig) {
        val configContainer = BVSourceSecretContainer(
            sourceType = config.sourceType,
            sourceName = config.sourceName,
            user = config.user,
            secretToken = jsonDeserializer.serializeToString(config)
        )
        secretsCollectionRef.document(config.sourceName).set(configContainer).get()
    }

    @CacheEvict(BVCacheNames.SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun delete(sourceName: String) {
        secretsCollectionRef.document(sourceName)
            .delete()
            .get()
    }

    private fun deserialize(secretToken: String): BVAbstractSourceConfig? {
        try {
            return jsonDeserializer.deserializeString(secretToken, BVAbstractSourceConfig::class.java)
        } catch (e: Exception) {
            log.error("", e)
            return null
        }
    }
}
