package org.birdview.storage.firebase

import com.google.cloud.firestore.CollectionReference
import javax.inject.Named

@Named
class BVFireCollectionAccessor(
    val clientProvider: BVFirebaseClientProvider
) {
    fun getUserCollection(): CollectionReference =
        clientProvider.getClientForCollection("users")

    fun getUserSourceCredentialsCollection(bvUser: String): CollectionReference =
        getUserCollection().document(bvUser).collection("sourceCredentials")

    fun getDefaultRefreshTokensCollection(): CollectionReference =
        clientProvider.getClientForCollection("oauthDefaultRefreshTokens")

    fun getStorageSecretsCollection(): CollectionReference =
        clientProvider.getClientForCollection("storageSecrets")

    fun getUserSourcesCollection(bvUser: String) =
        getUserCollection().document(bvUser).collection("sources")
}