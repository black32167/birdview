package org.birdview.storage.firebase

import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import javax.annotation.PostConstruct
import javax.inject.Named

@Named
class BVFirebaseClientProvider {
    companion object {
        private val app = FirebaseApp.initializeApp()
    }

    private lateinit var delegateClient: Firestore;

    @PostConstruct
    private fun init() {
        delegateClient = FirestoreClient.getFirestore(app)
    }

    fun getClientForCollection(collectionName: String) =
        delegateClient.collection(collectionName)

    fun getClient() = delegateClient
}