package org.birdview.storage.firebase

import org.birdview.BVProfiles
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.springframework.context.annotation.Profile
import javax.inject.Named

@Profile(BVProfiles.FIRESTORE)
@Named
class BVFireUserStorage(clientProvider: BVFirebaseClientProvider): BVUserStorage {
    private val userCollectionRef = clientProvider.getClientForCollection("users")

    override fun listUserNames(): List<String> {
        return userCollectionRef.listDocuments()
            .map { doc -> doc.id }
    }

    override fun create(userName: String, userSettings: BVUserSettings) {
        update(userName, userSettings)
    }

    override fun update(userName: String, userSettings: BVUserSettings) {
        userCollectionRef.document(userName).set(userSettings)
    }

    override fun getUserSettings(userName: String): BVUserSettings =
        userCollectionRef
            .document(userName).get().get()
            .toObject(BVUserSettings::class.java)!!

    override fun updateUserStatus(userName: String, enabled: Boolean) {
        userCollectionRef
            .document(userName)
            .update(BVUserSettings::enabled.name, true)
    }

    override fun delete(userName: String) {
        userCollectionRef
            .document(userName)
            .delete()
    }
}
