package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.model.secrets.BVAbstractSourceSecret

interface BVTaskSource {
    fun getTasks(bvUser: String, updatedPeriod: TimeIntervalFilter, sourceConfig: BVAbstractSourceSecret, chunkConsumer: BVSessionDocumentConsumer)
    fun getType(): SourceType
    fun canHandleId(id: String): Boolean = false
    fun loadByIds(sourceName: String, keyList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
    }
    fun resolveSourceUserId(sourceName:String, email: String):String = email
}