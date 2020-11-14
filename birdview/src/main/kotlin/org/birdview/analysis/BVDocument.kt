package org.birdview.analysis

import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.UserRole
import org.birdview.source.SourceType
import java.util.*

data class BVDocument(
        val ids: Set<BVDocumentId>,
        var title: String,
        val key: String,
        val body: String = "",
        val updated: Date? = null,
        val created: Date? = null,
        val closed: Date? = null,
        val httpUrl: String,
        var users: List<BVDocumentUser> = listOf(),
        val refs: List<BVDocumentRef> = emptyList(),
        val status: BVDocumentStatus?,
        val operations: List<BVDocumentOperation> = emptyList(),
        val sourceType: SourceType,
        val priority: Priority = Priority.NORMAL
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
}

enum class Priority {
    LOW,
    NORMAL,
    HIGH
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
        val authorDisplayName: String? = null,
        val created: Date?,
        val sourceName: String,
        val type: BVDocumentOperationType = BVDocumentOperationType.COMMENT
)

data class BVDocumentId(
        val id:String,
        val type:String,
        val sourceName:String)