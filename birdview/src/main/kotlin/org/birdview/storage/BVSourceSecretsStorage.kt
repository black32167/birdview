package org.birdview.storage

import org.birdview.storage.model.secrets.BVAbstractSourceSecret

interface BVSourceSecretsStorage {
    fun getSecret(sourceName: String): BVAbstractSourceSecret?

    fun <T: BVAbstractSourceSecret> getSecret(sourceName: String, configClass: Class<T>) : T?

    fun getSecrets(): List<BVAbstractSourceSecret>

    fun listSourceNames(): List<String>

    fun create(config: BVAbstractSourceSecret)

    fun update(config: BVAbstractSourceSecret)

    fun delete(sourceName: String)
}

