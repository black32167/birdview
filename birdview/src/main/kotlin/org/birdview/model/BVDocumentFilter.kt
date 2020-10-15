package org.birdview.model

import java.time.ZonedDateTime

data class BVDocumentFilter(
        val reportType: ReportType,
        val grouping: Boolean,
        val updatedPeriod: TimeIntervalFilter,
        val userFilter: UserFilter,
        val sourceType:String? = null,
        val representationType: RepresentationType = RepresentationType.LIST
)

enum class UserRole {
    CREATOR,
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