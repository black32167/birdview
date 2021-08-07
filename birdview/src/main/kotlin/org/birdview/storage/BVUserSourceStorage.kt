package org.birdview.storage

import org.birdview.storage.model.BVSourceConfig

interface BVUserSourceStorage {
    fun getSource(bvUser: String, sourceName: String): BVSourceConfig?

    fun create(bvUser: String, sourceName: String, sourceUserName: String)

    fun update(bvUser: String, sourceConfig: BVSourceConfig)

    fun listSourceNames(bvUser: String): List<String>

    fun listSourceProfiles(bvUser: String): List<BVSourceConfig>

    fun delete(bvUser: String, sourceName: String)

    fun deleteAll(bvUser: String)
}

