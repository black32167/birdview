package org.birdview.storage

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter

interface BVDocumentStorage {
    fun findDocuments(filter: BVDocumentFilter): List<BVDocument>
    fun getDocuments(externalDocsIds: Collection<String>): List<BVDocument>
    fun updateDocument(doc: BVDocument, bvUser: String)
    fun count(): Int
    fun removeExistingExternalIds(externalIds: List<String>): List<String>
    fun getReferringDocuments(externalIds: Set<String>): List<BVDocument>
}