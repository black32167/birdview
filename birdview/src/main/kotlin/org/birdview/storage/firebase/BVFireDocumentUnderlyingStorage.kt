package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentSnapshot
import org.birdview.model.BVDocumentFilter

interface BVFireDocumentUnderlyingStorage {
    fun updateDocument(persistent: BVFirePersistingDocument)
    fun findDocuments(filter: BVDocumentFilter): List<DocumentSnapshot>
    fun getDocumentsByExternalIds(externalDocsIds: Collection<String>): List<DocumentSnapshot>
    fun getReferringDocumentsByRefIds(externalDocsIds: Collection<String>): List<DocumentSnapshot>
}