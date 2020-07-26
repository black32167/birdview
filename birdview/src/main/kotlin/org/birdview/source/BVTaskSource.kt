package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter

interface BVTaskSource {
    fun getTasks(user: String?, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit)
    fun getType(): String
    fun canHandleId(id: String): Boolean = false
    fun loadByIds(list: List<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
    }
}