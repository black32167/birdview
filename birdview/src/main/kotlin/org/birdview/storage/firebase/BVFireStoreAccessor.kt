package org.birdview.storage.firebase

import com.google.cloud.firestore.CollectionReference
import javax.inject.Named

@Named
class BVFireStoreAccessor(
    val clientProvider: BVFirebaseClientProvider
) {
    fun db() = clientProvider.getClient()

    fun getUserCollection(): CollectionReference =
        clientProvider.getClientForCollection(BVFireCollections.USERS)

    fun getRefreshTokensCollection(bvUser: String): CollectionReference =
        getUserCollection().document(bvUser).collection(BVFireCollections.OAUTH_REFRESH_TOKENS)

    fun getUserSourcesCollection(bvUser: String) =
        getUserCollection().document(bvUser).collection(BVFireCollections.USER_SOURCES)
}