package org.birdview.time

import java.time.OffsetDateTime
import javax.inject.Named

@Named
class RealTimeService: BVTimeService {
    override fun getNow(): OffsetDateTime = OffsetDateTime.now()
}