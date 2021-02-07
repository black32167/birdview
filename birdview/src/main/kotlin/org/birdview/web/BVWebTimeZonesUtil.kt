package org.birdview.web

import java.time.ZoneId

object BVWebTimeZonesUtil {
    fun getAvailableTimezoneIds() =
        ZoneId.getAvailableZoneIds().sorted()
}