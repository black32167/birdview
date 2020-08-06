package org.birdview.analysis

import org.birdview.model.BVDocumentStatus
import org.birdview.model.UserRole
import java.util.*

data class BVDocument (
        val ids: Set<BVDocumentId>,
        var title: String,
        val key: String,
        val body: String = "",
        val updated: Date? = null,
        val created: Date? = null,
        val closed: Date? = null,
        val httpUrl: String,
        var users: List<BVDocumentUser> = listOf(),
        val subDocuments: MutableList<BVDocument> = mutableListOf(),
        val groupIds: Set<BVDocumentId> = emptySet(),
        val refsIds: Set<String> = emptySet(),
        val status: BVDocumentStatus?,
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

data class BVDocumentUser(
        val userName: String,
        val role: UserRole
)

class BVDocumentOperation (
        val description: String,
        val author: String,
        val created: Date?,
        val sourceName: String
)

data class BVDocumentId(
        val id:String,
        val type:String,
        val sourceName:String)