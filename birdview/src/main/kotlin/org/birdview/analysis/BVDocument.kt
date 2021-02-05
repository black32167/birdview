package org.birdview.analysis

import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.UserRole
import org.birdview.source.SourceType
import java.time.OffsetDateTime
import java.util.*

data class BVDocument (
        val ids: Set<BVDocumentId>,
        val title: String,
        val key: String,
        val body: String = "",
        val updated: OffsetDateTime? = null,
        val created: OffsetDateTime? = null,
        val closed: OffsetDateTime? = null,
        val httpUrl: String,
        val users: List<BVDocumentUser> = listOf(),
        val refs: List<BVDocumentRef> = emptyList(),
        val status: BVDocumentStatus?,
        val operations: List<BVDocumentOperation> = emptyList(),
        val sourceType: SourceType,
        val sourceName: String,
        val priority: Priority = Priority.NORMAL,
        val internalId: String = UUID.randomUUID().toString()
)

enum class Priority {
    LOW,
    NORMAL,
    HIGH
}

data class BVDocumentUser (
        val userName: String,
        val role: UserRole,
        val sourceName: String
)

enum class BVDocumentOperationType {
    COMMENT, UPDATE, NONE
}

class BVDocumentOperation (
        val description: String,
        val author: String,
        val authorDisplayName: String? = null,
        val created: OffsetDateTime?,
        val sourceName: String,
        val type: BVDocumentOperationType = BVDocumentOperationType.COMMENT
)

data class BVDocumentId(
        val id: String,
        val sourceType: SourceType? = null
)