package org.birdview.storage.firebase

import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.model.BVUserSourceConfig
import org.springframework.context.annotation.Profile
import javax.inject.Named

@Profile(BVProfiles.FIRESTORE)
@Named
class BVFireUserSourceStorage(clienProvider: BVFirebaseClientProvider): BVUserSourceStorage {
    private val userCollectionRef = clienProvider.getClientForCollection("users")

    override fun getSourceProfile(bvUserName: String, sourceName: String): BVUserSourceConfig =
        getUserSourcesCollectionRef(bvUserName)
            .document(sourceName).get().get()
            .toObject(BVUserSourceConfig::class.java)!!

    override fun create(bvUserName: String, sourceName: String, sourceUserName: String) {
        update(
            bvUserName = bvUserName,
            sourceName = sourceName,
            userProfileSourceConfig = BVUserSourceConfig(sourceUserName, "" != sourceUserName))
    }

    override fun update(bvUserName: String, sourceName: String, userProfileSourceConfig: BVUserSourceConfig) {
        getUserSourcesCollectionRef(bvUserName)
            .document(sourceName)
            .set(userProfileSourceConfig)
    }

    override fun listUserSources(bvUserName: String): List<String> =
        getUserSourcesCollectionRef(bvUserName)
            .listDocuments()
            .map { it.id }

    override fun delete(bvUserName: String, sourceName: String) {
        getUserSourcesCollectionRef(bvUserName)
            .document(sourceName)
            .delete()
    }

    override fun deleteAll(bvUserName: String) {
        getUserSourcesCollectionRef(bvUserName)
            .listDocuments()
            .forEach { it.delete() }
    }

    private fun getUserSourcesCollectionRef(bvUserName: String) =
        userCollectionRef.document(bvUserName).collection("sources")
}