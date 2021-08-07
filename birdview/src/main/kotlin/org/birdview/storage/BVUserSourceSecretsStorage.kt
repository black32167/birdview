package org.birdview.storage

import org.birdview.storage.model.secrets.BVAbstractSourceSecret

interface BVUserSourceSecretsStorage {
    fun getSecret(bvUser:String, sourceName: String): BVAbstractSourceSecret?

    fun getSecrets(bvUser:String): List<BVAbstractSourceSecret>

    fun create(bvUser:String, config: BVAbstractSourceSecret)

    fun update(bvUser:String, config: BVAbstractSourceSecret)

    fun delete(bvUser:String, sourceName: String)
}