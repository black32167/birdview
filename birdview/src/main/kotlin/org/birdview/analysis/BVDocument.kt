package org.birdview.analysis

import java.util.*

open class BVDocument (
        val ids: Set<BVDocumentId>,
        var title: String,
        val key: String,
        val body: String = "",
        val updated: Date? = null,
        val created: Date? = null,
        val httpUrl: String,
        val subDocuments: MutableList<BVDocument> = mutableListOf(),
        val groupIds: Set<BVDocumentId> = emptySet(),
        val refsIds: Set<String> = emptySet(),
        val status: String,
        val operations: List<BVDocumentOperation> = emptyList()
) {
    val inferredIds: MutableSet<BVDocumentId> = mutableSetOf<BVDocumentId>()
            .apply { addAll(ids) }

    fun addDocument(task:BVDocument) {
        subDocuments.add(task)
        inferredIds.addAll(task.groupIds)
    }

    fun getLastUpdated(): Date? =
            subDocuments.mapNotNull { it.updated }.min()
}

class BVDocumentOperation (
        val description: String,
        val author: String,
        val created: Date
)

data class BVDocumentId(
        val id:String,
        val type:String,
        val sourceName:String)