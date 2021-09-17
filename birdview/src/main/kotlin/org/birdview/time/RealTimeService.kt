package org.birdview.time

import org.birdview.storage.BVUserStorage
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Named

@Named
class RealTimeService(
    private val userStorage: BVUserStorage
): BVTimeService {
    override fun getNow(zoneId:ZoneId): OffsetDateTime = OffsetDateTime.now(zoneId)

    override fun getTodayInUserZone(bvUser: String): OffsetDateTime {
        val userProfile = userStorage.getUserSettings(bvUser)
        val todayInUserTz = getNow(ZoneId.of(userProfile.zoneId)).truncatedTo(ChronoUnit.DAYS)
        return todayInUserTz
//        val todayUtc = OffsetDateTime.ofInstant(todayInUserTz.toInstant(), ZoneId.of("UTC"))
//        return todayUtc
    }
}