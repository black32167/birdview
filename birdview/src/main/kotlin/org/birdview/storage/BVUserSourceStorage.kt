package org.birdview.storage

import org.birdview.storage.model.BVUserSourceConfig

interface BVUserSourceStorage {
    fun getBVUserNameBySourceUserName(userAlias: String?, sourceName: String): String

    fun getSourceProfile(bvUserName: String, sourceName: String): BVUserSourceConfig

    fun create(bvUserName: String, sourceName: String, sourceUserName:String)

    fun update(bvUserName: String, sourceName: String, userProfileSourceConfig: BVUserSourceConfig)

    fun listUserSources(userName: String): List<String>
}

