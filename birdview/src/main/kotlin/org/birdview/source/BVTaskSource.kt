package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter

interface BVTaskSource {
    fun getTasks(user: String, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit)
    fun getType(): SourceType
    fun canHandleId(id: String): Boolean = false
    fun loadByIds(sourceName: String, keyList: List<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
    }
    fun isAuthenticated(sourceName: String): Boolean
}