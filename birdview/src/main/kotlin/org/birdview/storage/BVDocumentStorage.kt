package org.birdview.storage

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentRef

interface BVDocumentStorage {
    fun findDocuments(filter: BVDocumentFilter): List<BVDocument>
    fun getDocuments(searchingDocsIds: Collection<String>): List<BVDocument>
    fun updateDocument(doc: BVDocument, bvUser: String)
    fun count(): Int
    fun containsDocWithExternalId(externalId: String): Boolean
    fun getIncomingRefsByExternalIds(externalIds: Set<String>): List<BVDocumentRef>
}