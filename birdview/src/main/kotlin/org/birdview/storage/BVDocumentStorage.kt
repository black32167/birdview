package org.birdview.storage

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import java.time.OffsetDateTime

interface BVDocumentStorage {
    fun getLastUpdatedDocument(bvUser: String, sourceName: String): OffsetDateTime?
    fun findDocuments(filter: BVDocumentFilter): List<BVDocument>
    fun getDocuments(bvUser:String, externalDocsIds: Collection<String>): List<BVDocument>
    fun updateDocument(bvUser: String, doc: BVDocument)
    fun removeExistingExternalIds(bvUser:String, externalIds: List<String>): List<String>
    fun getReferringDocuments(bvUser:String, externalIds: Set<String>): List<BVDocument>
}