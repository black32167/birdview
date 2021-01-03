package org.birdview.source.http.log.replay

import org.birdview.config.BVFoldersConfig
import org.birdview.time.BVTimeService
import org.birdview.utils.BVDateTimeUtils
import java.nio.file.Files
import java.time.ZonedDateTime

class BVFrozenTimeService(
    folderConfig: BVFoldersConfig
): BVTimeService {
    private val instant: ZonedDateTime = BVDateTimeUtils.parse(Files.readString(folderConfig.getSingularTimeFile()))

    override fun getNow(): ZonedDateTime =
        instant
}