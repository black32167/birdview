package org.birdview.storage.firebase

import org.birdview.BVProfiles
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.model.BVSourceSecretContainer
import org.birdview.utils.JsonDeserializer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import javax.inject.Named

@Profile(BVProfiles.FIRESTORE)
@Named
open class BVFireSourceSecretsStorage(
    clientProvider: BVFirebaseClientProvider,
    open val jsonDeserializer: JsonDeserializer
) : BVSourceSecretsStorage {
    private val log = LoggerFactory.getLogger(BVFireSourceSecretsStorage::class.java)

    private val secretsCollectionRef = clientProvider.getClientForCollection("storageSecrets")

    override fun getConfigByName(sourceName: String): BVAbstractSourceConfig? =
        secretsCollectionRef
            .document(sourceName).get().get()
            ?.let { it.getString(BVSourceSecretContainer::secretToken.name)!! }
            ?.let { deserialize(it) }

    override fun <T : BVAbstractSourceConfig> getConfigByName(sourceName: String, configClass: Class<T>): T? =
        getConfigByName(sourceName)
            .let { configClass.cast(it) }

    override fun listSourceNames(): List<String> =
        secretsCollectionRef.listDocuments()
            .map { it.id }

    override fun create(config: BVAbstractSourceConfig) {
        update(config)
    }

    override fun update(config: BVAbstractSourceConfig) {
        val configContainer = BVSourceSecretContainer(
            sourceType = config.sourceType,
            sourceName = config.sourceName,
            user = config.user,
            secretToken = jsonDeserializer.serializeToString(config)
        )
        secretsCollectionRef.document(config.sourceName).set(configContainer).get()
    }

    override fun delete(sourceName: String) {
        secretsCollectionRef.document(sourceName)
            .delete()
            .get()
    }

    private fun deserialize(secretToken: String): BVAbstractSourceConfig? {
        try {
            return jsonDeserializer.deserializeString(secretToken, BVAbstractSourceConfig::class.java)
        } catch (e: Exception) {
            log.error("", e)
            return null
        }
    }
}
