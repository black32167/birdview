package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.utils.BVDateTimeUtils
import org.birdview.web.explore.model.BVDocumentView

object BVDocumentViewFactory {
    fun create(doc: BVDocument) : BVDocumentView =
            BVDocumentView(
                    internalId = doc.internalId,
                    ids = doc.ids.map { it.id },
                    httpUrl = doc.httpUrl,
                    sourceName = doc.sourceName,
                    status = doc.status?.name ?: "???",
                    title = doc.title,
                    updated = BVDateTimeUtils.format(doc.updated, "dd-MM-yyyy"),
                    key = doc.key,
                    lastUpdater = doc.operations.firstOrNull()
                            ?.run { authorDisplayName?:author },
                    priority = doc.priority
            )
}