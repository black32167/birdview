package org.birdview.storage.firebase

import org.birdview.BVCacheNames
import org.birdview.BVProfiles
import org.birdview.storage.OAuthTokenStorage
import org.birdview.storage.model.BVOAuthTokens
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import javax.inject.Named

@Profile(BVProfiles.FIRESTORE)
@Named
open class BVFirebaseOAuthTokenStorage(
    open val collectionAccessor: BVFireStoreAccessor
) : OAuthTokenStorage {
    @Cacheable(BVCacheNames.SOURCE_OAUTH_TOKENS_CACHE_NAME)
    override fun loadOAuthTokens(bvUser: String, sourceName: String): BVOAuthTokens? =
        collectionAccessor.getRefreshTokensCollection(bvUser)
            .document(sourceName).get().get()
            .toObject(BVOAuthTokens::class.java);

    @CacheEvict(BVCacheNames.SOURCE_OAUTH_TOKENS_CACHE_NAME, key = "#sourceName")
    override fun saveOAuthTokens(bvUser: String, sourceName: String, tokens: BVOAuthTokens) {
        collectionAccessor.getRefreshTokensCollection(bvUser)
            .document(sourceName).set(tokens).get()
    }
}