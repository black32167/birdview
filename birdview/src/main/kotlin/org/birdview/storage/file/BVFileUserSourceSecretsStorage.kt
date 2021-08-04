package org.birdview.storage.file

import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceSecretsStorage
import org.birdview.storage.model.secrets.BVAbstractSourceConfig
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FILESTORE)
@Repository
class BVFileUserSourceSecretsStorage: BVUserSourceSecretsStorage {
    override fun getSecret(bvUser: String, sourceName: String): BVAbstractSourceConfig? {
        TODO("Not yet implemented")
    }

    override fun getSecrets(bvUser: String): List<BVAbstractSourceConfig> {
        TODO("Not yet implemented")
    }

    override fun create(bvUser: String, config: BVAbstractSourceConfig) {
        TODO("Not yet implemented")
    }

    override fun update(bvUser: String, config: BVAbstractSourceConfig) {
        TODO("Not yet implemented")
    }

    override fun delete(bvUser: String, sourceName: String) {
        TODO("Not yet implemented")
    }
}