package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVAbstractSourceConfig

interface BVTaskSource {
    fun getTasks(user: String, updatedPeriod: TimeIntervalFilter, sourceConfig: BVAbstractSourceConfig, chunkConsumer: (List<BVDocument>) -> Unit)
    fun getType(): SourceType
    fun canHandleId(id: String): Boolean = false
    fun loadByIds(sourceName: String, keyList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
    }
    fun resolveSourceUserId(sourceName:String, email: String):String = email
    fun isAuthenticated(sourceName: String): Boolean
}