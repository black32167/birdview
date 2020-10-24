package org.birdview.web.explore

import org.birdview.analysis.BVDocument
import org.birdview.utils.BVDateTimeUtils
import org.birdview.web.explore.model.BVDocumentView
import java.util.*

object BVDocumentViewFactory {
    fun create(doc: BVDocument) : BVDocumentView =
            BVDocumentView(
                    id = UUID.randomUUID().toString(),
                    ids = doc.ids.map { it.id },
                    httpUrl = doc.httpUrl,
                    sourceName = doc.ids
                            .firstOrNull()
                            ?.sourceName
                            ?: "???",
                    status = doc.status?.let { it.name } ?: "???",
                    title = doc.title,
                    updated = BVDateTimeUtils.format(doc.updated, "dd-MM-yyyy"),
                    key = doc.key,
                    lastUpdater = doc.operations.firstOrNull()?.author,
                    priority = doc.priority
            )

}