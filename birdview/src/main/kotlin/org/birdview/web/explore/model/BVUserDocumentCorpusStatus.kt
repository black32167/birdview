package org.birdview.web.explore.model

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class BVUserDocumentCorpusStatus {
    private var lastUpdated: String? = null
    private var updating: Boolean = true

    @Synchronized
    fun isUpdating() = updating

    @Synchronized
    fun startUpdating() {
        this.updating = true
    }

    @Synchronized
    fun finishUpdating() {
        this.updating = false
        this.lastUpdated = stringTS()
    }

    private fun stringTS() =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(Instant.now())
}