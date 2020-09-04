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
    val lastOperations: List<BVDocumentOperation> = getLastUsersOperations(operations)

    private fun getLastUsersOperations(operations: List<BVDocumentOperation>): List<BVDocumentOperation> {
        data class OperationUser (val user: String, val source: String)
        val encounteredUsers = mutableSetOf<OperationUser>()
        val lastUsersOperations = mutableListOf<BVDocumentOperation>()
        for (operation in operations) {
            val user = OperationUser(operation.author, operation.sourceName)
            if (!encounteredUsers.contains(user)) {
                encounteredUsers.add(user)
                lastUsersOperations.add(operation)
            }
        }
        return lastUsersOperations
    }

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
        val role: UserRole,
        val sourceName: String
)

enum class BVDocumentOperationType {
    COMMENT, COLLABORATE, NONE
}

class BVDocumentOperation (
        val description: String,
        val author: String,
        val created: Date?,
        val sourceName: String,
        val type: BVDocumentOperationType = BVDocumentOperationType.COMMENT
)

data class BVDocumentId(
        val id:String,
        val type:String,
        val sourceName:String)