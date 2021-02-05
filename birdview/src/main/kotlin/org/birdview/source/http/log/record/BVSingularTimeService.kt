package org.birdview.source.http.log.record

import org.birdview.config.BVFoldersConfig
import org.birdview.time.BVTimeService
import org.birdview.utils.BVDateTimeUtils
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.OffsetDateTime

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

    override fun getNow(): OffsetDateTime = instant
}
