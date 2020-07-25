package org.birdview.source

import org.birdview.analysis.BVDocument
import org.birdview.model.UserFilter

interface BVTaskSource {
    fun getTasks(userFilters: List<UserFilter>):List<BVDocument>
    fun getType(): String
    fun canHandleId(id: String): Boolean = false
    fun loadByIds(list: List<String>) = listOf<BVDocument>()
}