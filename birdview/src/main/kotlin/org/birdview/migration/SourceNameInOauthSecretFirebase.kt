package org.birdview.migration

import com.google.cloud.firestore.DocumentReference
import org.birdview.storage.firebase.BVFireStoreAccessor
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.utils.JsonMapper
import org.slf4j.LoggerFactory

//@Named
class SourceNameInOauthSecretFirebase(
    private val db: BVFireStoreAccessor,
    private val jsonMapper: JsonMapper
): MigrationTask {
    private val log = LoggerFactory.getLogger(SourceNameInOauthSecretFirebase::class.java)

    data class BVOMigratingAuthSourceSecret (
        val _secretType: String,
        val sourceName:String?,
        val flavor: BVOAuthSourceSecret.OAuthFlavour,
        val clientId: String,
        val clientSecret: String,
        val authCodeUrl: String,
        val tokenExchangeUrl: String,
        val scope: String)


    override fun commence() {
        db.getUserCollection().listDocuments().forEach(this::updateUser)
    }

    private fun updateUser(doc: DocumentReference) {
        doc.collection("userSources").listDocuments()
            .forEach(this::updateUserSource)

    }

    private fun updateUserSource(sourceDocRef: DocumentReference) {
        val future = db.clientProvider.getClient().runTransaction {transaction ->
            val sourceDocSnapshot = transaction.get(sourceDocRef).get()
            sourceDocSnapshot.getString("serializedSourceSecret")?.also { serializedSecret->
                val secret = jsonMapper.deserializeString(serializedSecret, BVOMigratingAuthSourceSecret::class.java)
                if (secret._secretType == "oauth") {
                    val sourceName = sourceDocSnapshot.getString("sourceName")!!
                    val updatedSecret = secret.copy(sourceName = sourceName)
                    val updatedSerializedSecret = jsonMapper.serializeToString(updatedSecret)
                    transaction.update(sourceDocRef, "serializedSourceSecret", updatedSerializedSecret)
                }
            }
        }

        try {
            val txResult = future.get()
            log.info("Updated: ${txResult}")
        } catch (e: Exception) {
            log.info("Failed:", e)
        }
    }
}