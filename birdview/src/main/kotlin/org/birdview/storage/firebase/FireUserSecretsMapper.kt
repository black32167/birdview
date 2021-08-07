package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentSnapshot
import org.birdview.storage.model.BVSourceSecretContainer
import org.birdview.storage.model.secrets.BVAbstractSourceSecret
import org.birdview.utils.JsonDeserializer
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class FireUserSecretsMapper(val jsonDeserializer: JsonDeserializer) {
    private val log = LoggerFactory.getLogger(FireUserSecretsMapper::class.java)

    fun extractSecrets(doc: DocumentSnapshot): BVAbstractSourceSecret? =
        doc.getString(BVSourceSecretContainer::secretToken.name)
            ?.let { deserialize(it) }

    fun toContainer(secret: BVAbstractSourceSecret) =
        BVSourceSecretContainer(
            sourceType = secret.sourceType,
            sourceName = secret.sourceName,
            user = secret.user,
            secretToken = jsonDeserializer.serializeToString(secret)
        )

    private fun deserialize(secretToken: String): BVAbstractSourceSecret? {
        try {
            return jsonDeserializer.deserializeString(secretToken, BVAbstractSourceSecret::class.java)
        } catch (e: Exception) {
            log.error("", e)
            return null
        }
    }
}