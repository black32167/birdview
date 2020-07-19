package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter

interface BVTaskSource {
    fun getTasks(request: BVDocumentFilter):List<BVDocument>
    fun getType(): String
    fun canHandleId(id: String): Boolean = false
    fun loadByIds(list: List<String>) = listOf<BVDocument>()
}