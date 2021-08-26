package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter

interface BVTaskSource {
    fun getTasks(bvUser: String, updatedPeriod: TimeIntervalFilter, sourceConfig: BVSourceConfigProvider.SyntheticSourceConfig, chunkConsumer: BVSessionDocumentConsumer)
    fun getType(): SourceType
    fun canHandleId(id: String): Boolean = false
    fun loadByIds(bvUser: String, sourceName: String, keyList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
    }
    fun resolveSourceUserId(bvUser: String, sourceName: String, email: String):String = email
}