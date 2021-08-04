package org.birdview.storage

import org.birdview.storage.model.secrets.BVAbstractSourceConfig

interface BVUserSourceSecretsStorage {
    fun getSecret(bvUser:String, sourceName: String): BVAbstractSourceConfig?

    fun getSecrets(bvUser:String): List<BVAbstractSourceConfig>

    fun create(bvUser:String, config: BVAbstractSourceConfig)

    fun update(bvUser:String, config: BVAbstractSourceConfig)

    fun delete(bvUser:String, sourceName: String)
}