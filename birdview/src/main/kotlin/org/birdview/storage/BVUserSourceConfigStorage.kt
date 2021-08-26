package org.birdview.storage

import org.birdview.storage.model.source.config.BVUserSourceConfig

interface BVUserSourceConfigStorage {
    fun getSource(bvUser: String, sourceName: String): BVUserSourceConfig?

    fun create(bvUser: String, sourceConfig: BVUserSourceConfig)

    // TODO: introduce BVUserSourceConfigUpdate to represent update delta.
    fun update(bvUser: String, sourceConfig: BVUserSourceConfig)

    fun listSourceNames(bvUser: String): List<String>

    fun listSources(bvUser: String): List<BVUserSourceConfig>

    fun delete(bvUser: String, sourceName: String)

    fun deleteAll(bvUser: String)
}

