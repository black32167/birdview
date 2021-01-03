package org.birdview.time

import java.time.ZonedDateTime
import javax.inject.Named

@Named
class RealTimeService: BVTimeService {
    override fun getNow(): ZonedDateTime = ZonedDateTime.now()
}