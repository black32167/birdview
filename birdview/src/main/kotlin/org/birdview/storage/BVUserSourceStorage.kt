package org.birdview.storage

import org.birdview.storage.model.BVUserSourceConfig

interface BVUserSourceStorage {
    fun getSourceProfile(bvUser: String, sourceName: String): BVUserSourceConfig

    fun create(bvUser: String, sourceName: String, sourceUserName: String)

    fun update(bvUser: String, userProfileSourceConfig: BVUserSourceConfig)

    fun listUserSources(bvUser: String): List<String>

    fun delete(bvUser: String, sourceName: String)

    fun deleteAll(bvUser: String)

    fun listUserSourceProfiles(bvUser: String): List<BVUserSourceConfig>
}

