package org.birdview.source.http.log.replay

import org.birdview.config.BVFoldersConfig
import org.birdview.time.BVTimeService
import org.birdview.utils.BVDateTimeUtils
import java.nio.file.Files
import java.time.OffsetDateTime
import java.time.ZoneId

class BVFrozenTimeService(
    folderConfig: BVFoldersConfig
): BVTimeService {
    private val instant: OffsetDateTime = BVDateTimeUtils.offsetParse(Files.readString(folderConfig.getSingularTimeFile()))

    override fun getNow(zoneId: ZoneId): OffsetDateTime =
        instant

    override fun getTodayInUserZone(bvUser: String): OffsetDateTime =
        instant
}