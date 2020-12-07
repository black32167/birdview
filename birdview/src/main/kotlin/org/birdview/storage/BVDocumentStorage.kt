package org.birdview.storage

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentFilter

interface BVDocumentStorage {
    fun findDocuments(filter: BVDocumentFilter): List<BVDocument>
    fun getDocuments(searchingDocsIds: Set<String>): List<BVDocument>
    fun updateDocument(doc: BVDocument)
    fun count(): Int
    fun containsDocWithExternalId(externalId: String): Boolean
}