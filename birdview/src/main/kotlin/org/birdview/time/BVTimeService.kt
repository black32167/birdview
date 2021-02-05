package org.birdview.time

import java.time.OffsetDateTime

interface BVTimeService {
    fun getNow(): OffsetDateTime
}
