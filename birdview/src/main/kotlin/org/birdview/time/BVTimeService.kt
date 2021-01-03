package org.birdview.time

import java.time.ZonedDateTime

interface BVTimeService {
    fun getNow(): ZonedDateTime
}
