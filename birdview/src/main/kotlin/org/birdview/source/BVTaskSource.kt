package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.request.TasksRequest

interface BVTaskSource {
    fun getTasks(request: TasksRequest):List<BVDocument>
    fun getType(): String
    fun canHadleId(id: String): Boolean = false
    fun loadByIds(list: List<String>) = listOf<BVDocument>()
}