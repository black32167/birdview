package org.birdview.request

import org.birdview.model.ReportType
import java.time.ZonedDateTime

class TasksRequest(
        val reportType: ReportType,
        val grouping: Boolean,
        val groupingThreshold: Double,
        val since: ZonedDateTime? = null,
        val user:String?,
        val sourceType:String?
)