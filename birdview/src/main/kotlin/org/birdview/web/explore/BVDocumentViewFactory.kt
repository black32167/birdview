package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.user.BVLoggedUserSettingsProvider
import org.birdview.utils.BVDateTimeUtils
import org.birdview.web.explore.model.BVDocumentView
import java.time.ZoneId
import javax.inject.Named

@Named
class BVDocumentViewFactory(private val userSettings: BVLoggedUserSettingsProvider) {
    fun create(doc: BVDocument) : BVDocumentView =
            BVDocumentView(
                    internalId = doc.internalId,
                    ids = doc.ids.map { it.id },
                    httpUrl = doc.httpUrl,
                    sourceName = doc.sourceName,
                    status = doc.status?.name ?: "",
                    title = doc.title,
                    updated = BVDateTimeUtils.format(
                            doc.updated?.atZoneSameInstant(getDisplayTimezoneId()), "dd-MM-yyyy"),
                    key = doc.key,
                    lastUpdater = doc.operations.firstOrNull()
                            ?.run { authorDisplayName?:author },
                    priority = doc.priority
            )

        private fun getDisplayTimezoneId() =
                ZoneId.of(userSettings.getTimezoneId())
}