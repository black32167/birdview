package org.birdview.storage

import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.utils.JsonMapper
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class SourceSecretsMapper(private val jsonMapper: JsonMapper) {
    private val log = LoggerFactory.getLogger(SourceSecretsMapper::class.java)

    fun serialize(secret: BVSourceSecret) =
        jsonMapper.serializeToString(secret)

    fun deserialize(secretToken: String): BVSourceSecret {
        return jsonMapper.deserializeString(secretToken, BVSourceSecret::class.java)
    }
}