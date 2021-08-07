package org.birdview.storage.file

import org.birdview.BVProfiles
import org.birdview.storage.BVUserSourceSecretsStorage
import org.birdview.storage.model.secrets.BVAbstractSourceSecret
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.FILESTORE)
@Repository
class BVFileUserSourceSecretsStorage: BVUserSourceSecretsStorage {
    override fun getSecret(bvUser: String, sourceName: String): BVAbstractSourceSecret? {
        TODO("Not yet implemented")
    }

    override fun getSecrets(bvUser: String): List<BVAbstractSourceSecret> {
        TODO("Not yet implemented")
    }

    override fun create(bvUser: String, config: BVAbstractSourceSecret) {
        TODO("Not yet implemented")
    }

    override fun update(bvUser: String, config: BVAbstractSourceSecret) {
        TODO("Not yet implemented")
    }

    override fun delete(bvUser: String, sourceName: String) {
        TODO("Not yet implemented")
    }
}