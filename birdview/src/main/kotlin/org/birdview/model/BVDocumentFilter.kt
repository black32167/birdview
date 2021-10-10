package org.birdview.model

import java.time.OffsetDateTime

data class BVDocumentFilter(
    val docStatuses: List<BVDocumentStatus>,
    val grouping: Boolean,
    val updatedPeriod: TimeIntervalFilter,
    val userFilter: UserFilter,
    val sourceName:String? = null,
    val representationType: RepresentationType = RepresentationType.LIST
)

enum class UserRole {
    COMMENTER,
    IMPLEMENTOR,
    WATCHER
}

data class UserFilter(
        val userAlias: String,
        val role: UserRole
)

data class TimeIntervalFilter(
    val after: OffsetDateTime? = null,
    val before: OffsetDateTime? = null
)