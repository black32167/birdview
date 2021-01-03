package org.birdview.model

import java.time.ZonedDateTime
import java.util.*

data class BVDocumentFilter(
        val docStatuses: List<BVDocumentStatus>,
        val grouping: Boolean,
        val updatedPeriod: TimeIntervalFilter,
        val userFilter: UserFilter,
        val sourceType:String? = null,
        val representationType: RepresentationType = RepresentationType.LIST
) {
    val filterId = UUID.randomUUID().toString()
}

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
    val after: ZonedDateTime? = null,
    val before: ZonedDateTime? = null
)