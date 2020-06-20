package org.birdview.request

import java.time.ZonedDateTime

class TasksRequest(
        val status: String,
        val grouping: Boolean,
        val groupingThreshold: Double,
        val since: ZonedDateTime,
        val user:String?,
        val sourceType:String?
)