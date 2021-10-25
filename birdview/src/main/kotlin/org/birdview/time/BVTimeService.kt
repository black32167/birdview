package org.birdview.time

import java.time.OffsetDateTime
import java.time.ZoneId

interface BVTimeService {
    companion object {
        val UTC = ZoneId.of("UTC")
    }
    fun getNow(zoneId:ZoneId = UTC): OffsetDateTime

    fun getTodayInUserZone(bvUser: String): OffsetDateTime
}
