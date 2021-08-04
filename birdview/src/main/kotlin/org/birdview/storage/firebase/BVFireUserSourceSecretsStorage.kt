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
    clientProvider: BVFirebaseClientProvider,
    open val secretsMapper: FireUserSecretsMapper
): BVUserSourceSecretsStorage {
    companion object {
        private const val USER_SOURCE_CREDENTIALS_COLLECTION = "sourceCredentials"
    }

    private val userCollectionRef = clientProvider.getClientForCollection("users")

    @Cacheable(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME)
    override fun getSecret(bvUser:String, sourceName: String): BVAbstractSourceConfig? =
        getUserSourceCredentialsCollectionRef(bvUser)
            .document(sourceName).get().get()
            ?.let { secretsMapper.extractSecrets(it) }


    @Cacheable(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME, key = "all")
    override fun getSecrets(bvUser:String): List<BVAbstractSourceConfig> =
        getUserSourceCredentialsCollectionRef(bvUser).get().get()
            .mapNotNull { secretsMapper.extractSecrets(it) }

    @CacheEvict(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun create(bvUser:String, config: BVAbstractSourceConfig) {
        update(bvUser, config)
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun update(bvUser:String, config: BVAbstractSourceConfig) {
        val configContainer = secretsMapper.toContainer(config)
        getUserSourceCredentialsCollectionRef(bvUser).document(config.sourceName).set(configContainer).get()
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME, allEntries = true)
    override fun delete(bvUser:String, sourceName: String) {
        getUserSourceCredentialsCollectionRef(bvUser).document(sourceName)
            .delete()
            .get()
    }

    private fun getUserSourceCredentialsCollectionRef(bvUserName: String) =
        userCollectionRef.document(bvUserName).collection(USER_SOURCE_CREDENTIALS_COLLECTION)
}