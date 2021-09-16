package org.birdview.time

import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Named

@Named
class RealTimeService: BVTimeService {
    override fun getNow(): OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC"))
}