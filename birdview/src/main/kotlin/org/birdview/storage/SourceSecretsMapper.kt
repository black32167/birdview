package org.birdview.storage

import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.utils.JsonDeserializer
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class SourceSecretsMapper(private val jsonDeserializer: JsonDeserializer) {
    private val log = LoggerFactory.getLogger(SourceSecretsMapper::class.java)

    fun serialize(secret: BVSourceSecret) =
        jsonDeserializer.serializeToString(secret)

    fun deserialize(secretToken: String): BVSourceSecret {
        return jsonDeserializer.deserializeString(secretToken, BVSourceSecret::class.java)
    }
}