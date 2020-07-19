package org.birdview.model

import java.time.ZonedDateTime

data class BVDocumentFilter(
        val reportType: ReportType,
        val grouping: Boolean,
        val since: ZonedDateTime? = null,
        val userFilters: List<UserFilter>,
        val sourceType:String? = null
)

enum class UserRole {
    CREATOR,
    IMPLEMENTOR,
    WATCHER
}
class UserFilter (
        val userAlias: String?,
        val role: UserRole
)