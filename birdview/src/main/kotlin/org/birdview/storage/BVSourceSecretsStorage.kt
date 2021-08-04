package org.birdview.storage

import org.birdview.storage.model.secrets.BVAbstractSourceConfig

interface BVSourceSecretsStorage {
    fun getSecret(sourceName: String): BVAbstractSourceConfig?

    fun <T: BVAbstractSourceConfig> getSecret(sourceName: String, configClass: Class<T>) : T?

    fun getSecrets(): List<BVAbstractSourceConfig>

    fun listSourceNames(): List<String>

    fun create(config: BVAbstractSourceConfig)

    fun update(config: BVAbstractSourceConfig)

    fun delete(sourceName: String)
}

