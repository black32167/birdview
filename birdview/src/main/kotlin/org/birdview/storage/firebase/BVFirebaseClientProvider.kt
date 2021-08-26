package org.birdview.storage.firebase

import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import javax.annotation.PostConstruct
import javax.inject.Named

@Named
class BVFirebaseClientProvider {
    private lateinit var delegateClient: Firestore;

    @PostConstruct
    private fun init() {
        val app = FirebaseApp.initializeApp()
        delegateClient = FirestoreClient.getFirestore(app)
    }

    fun getClientForCollection(collectionName: String) =
        delegateClient.collection(collectionName)

    fun getClient() = delegateClient
}