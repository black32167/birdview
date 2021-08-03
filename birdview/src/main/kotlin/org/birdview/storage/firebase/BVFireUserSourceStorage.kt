package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.BVAbstractSourceConfig
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
    override fun create(bvUser: String, sourceName: String, sourceUserName: String, bvSourceAccessConfig: BVAbstractSourceConfig?) {
        update(
            bvUser = bvUser,
            userProfileSourceConfig = BVUserSourceConfig(
                sourceName = sourceName, sourceUserName = sourceUserName, enabled = "" != sourceUserName, sourceConfig = bvSourceAccessConfig)
        )
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun update(bvUser: String, userProfileSourceConfig: BVUserSourceConfig) {
        getUserSourcesCollectionRef(bvUser)
            .document(userProfileSourceConfig.sourceName)
            .set(userProfileSourceConfig)
    }

    @Cacheable(cacheNames = [BVCacheNames.USER_SOURCE_CACHE], key = "sn:#bvUser" )
    override fun listUserSources(bvUser: String): List<String> =
        getUserSourcesCollectionRef(bvUser)
            .listDocuments()
            .map { it.id }

    @Cacheable(cacheNames = [BVCacheNames.USER_SOURCE_CACHE], key = "sc:#bvUser" )
    override fun listUserSourceProfiles(bvUser: String): List<BVUserSourceConfig> {
        return getUserSourcesCollectionRef(bvUser).get().get().toObjects(BVUserSourceConfig::class.java)
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun delete(bvUser: String, sourceName: String) {
        getUserSourcesCollectionRef(bvUser)
            .document(sourceName)
            .delete()
    }

    @CacheEvict(BVCacheNames.USER_SOURCE_CACHE, allEntries = true)
    override fun deleteAll(bvUser: String) {
        getUserSourcesCollectionRef(bvUser)
            .listDocuments()
            .forEach { it.delete() }
    }

    private fun getUserSourcesCollectionRef(bvUserName: String) =
        userCollectionRef.document(bvUserName).collection("sources")
}