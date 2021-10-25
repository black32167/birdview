package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentSnapshot
import org.birdview.model.BVDocumentFilter

interface BVFireDocumentUnderlyingStorage {
    fun updateDocument(persistent: BVFirePersistingDocument)
    fun findDocuments(filter: BVDocumentFilter): List<DocumentSnapshot>
    fun getDocumentsByExternalIds(bvUser:String, externalDocsIds: Collection<String>): List<DocumentSnapshot>
    fun getReferringDocumentsByRefIds(bvUser:String, externalDocsIds: Collection<String>): List<DocumentSnapshot>
    fun getLatestDocument(bvUser: String, sourceName: String): Long?
}