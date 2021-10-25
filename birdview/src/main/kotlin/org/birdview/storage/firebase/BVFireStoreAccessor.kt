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
        getUserReference(bvUser).collection(BVFireCollections.OAUTH_REFRESH_TOKENS)

    fun getUserSourcesCollection(bvUser: String) =
        getUserReference(bvUser).collection(BVFireCollections.USER_SOURCES)

    fun getDocumentsCollection(bvUser: String) =
        getUserReference(bvUser).collection(BVFireCollections.DOCUMENTS)

    private fun getUserReference(bvUser:String) =
        getUserCollection().document(bvUser)
}