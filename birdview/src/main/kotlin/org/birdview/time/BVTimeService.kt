package org.birdview.time

import java.time.OffsetDateTime
import java.time.ZoneId

interface BVTimeService {
    fun getNow(zoneId:ZoneId = ZoneId.of("UTC")): OffsetDateTime

    fun getTodayInUserZone(bvUser: String): OffsetDateTime
}
