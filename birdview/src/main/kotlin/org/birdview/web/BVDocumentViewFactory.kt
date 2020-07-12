package org.birdview.web

import org.birdview.analysis.BVDocument

object BVDocumentViewFactory {
    fun create(doc: BVDocument) : BVDocumentView =
            BVDocumentView(
                    subDocuments = doc.subDocuments.map(this::create),
                    httpUrl = doc.httpUrl,
                    sourceName = doc.ids
                            .firstOrNull()
                            ?.sourceName
                            ?: "???",
                    status = doc.status,
                    title = doc.title,
                    updated = doc.updated,
                    key = doc.key
            )

}