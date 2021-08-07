package org.birdview.storage

import org.birdview.storage.model.BVSourceConfig

interface BVUserSourceStorage {
    fun getSourceProfile(bvUser: String, sourceName: String): BVSourceConfig?

    fun create(bvUser: String, sourceName: String, sourceUserName: String)

    fun update(bvUser: String, userProfileSourceConfig: BVSourceConfig)

    fun listUserSources(bvUser: String): List<String>

    fun delete(bvUser: String, sourceName: String)

    fun deleteAll(bvUser: String)

    fun listUserSourceProfiles(bvUser: String): List<BVSourceConfig>
}

