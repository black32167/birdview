package org.birdview.source.http.log.replay

import org.birdview.config.BVFoldersConfig
import org.birdview.time.BVTimeService
import org.birdview.utils.BVDateTimeUtils
import java.nio.file.Files
import java.time.OffsetDateTime

class BVFrozenTimeService(
    folderConfig: BVFoldersConfig
): BVTimeService {
    private val instant: OffsetDateTime = BVDateTimeUtils.parse(Files.readString(folderConfig.getSingularTimeFile()))

    override fun getNow(): OffsetDateTime =
        instant
}