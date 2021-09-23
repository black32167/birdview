package org.birdview.storage.firebase

import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient

object BVFireUtil {
    private val fireClient: Firestore

    init {
        val app = FirebaseApp.initializeApp()
        fireClient = FirestoreClient.getFirestore(app)
    }

    fun copyUserSettings(bvUserFrom:String, bvUserTo:String) {
        val fromSourcesCollRef = fireClient
            .collection(BVFireCollections.USERS)
            .document(bvUserFrom)
            .collection(BVFireCollections.USER_SOURCES)
        val toSourcesCollRef = fireClient
            .collection(BVFireCollections.USERS)
            .document(bvUserTo)
            .collection(BVFireCollections.USER_SOURCES)

        fromSourcesCollRef.listDocuments().forEach { fromSourceRef ->
            val fromSource = fromSourceRef.get().get()
            toSourcesCollRef.document(fromSourceRef.id).create(fromSource.data!!).get()
        }
    }
}

fun main(vararg args:String) {
    val (bvFromUser, bvToUser) = args
    BVFireUtil.copyUserSettings(bvFromUser, bvToUser)
}
