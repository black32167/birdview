package org.birdview.source.gdrive

import java.time.ZonedDateTime

class GDriveFilesFilter (
        val since: ZonedDateTime,
        val user: String? = null
)