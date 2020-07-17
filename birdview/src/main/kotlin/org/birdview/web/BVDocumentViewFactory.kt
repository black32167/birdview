package org.birdview.web

import org.birdview.analysis.BVDocument
import java.util.*

object BVDocumentViewFactory {
    fun create(doc: BVDocument) : BVDocumentView =
            BVDocumentView(
                    id = UUID.randomUUID().toString(),
                    subDocuments = doc.subDocuments.map(this::create),
                    httpUrl = doc.httpUrl,
                    sourceName = doc.ids
                            .firstOrNull()
                            ?.sourceName
                            ?: "???",
                    status = doc.status ?.let { it.name } ?: "???",
                    title = doc.title,
                    updated = doc.updated,
                    key = doc.key
            )

}