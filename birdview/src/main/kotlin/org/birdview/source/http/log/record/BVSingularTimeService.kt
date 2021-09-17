package org.birdview.source.http.log.record

import org.birdview.config.BVFoldersConfig
import org.birdview.time.BVTimeService
import org.birdview.utils.BVDateTimeUtils
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.OffsetDateTime
import java.time.ZoneId

class BVSingularTimeService(
    val delegate: BVTimeService,
    folderConfig: BVFoldersConfig,
): BVTimeService {
    private var instant = delegate.getNow()

    init {
        Files.writeString(
            folderConfig.getSingularTimeFile(),
            BVDateTimeUtils.dateTimeFormat(instant),
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE
        )
    }

    override fun getNow(zoneId: ZoneId): OffsetDateTime = OffsetDateTime.ofInstant(instant.toInstant(), zoneId)

    override fun getTodayInUserZone(bvUser: String): OffsetDateTime = getNow()
}
