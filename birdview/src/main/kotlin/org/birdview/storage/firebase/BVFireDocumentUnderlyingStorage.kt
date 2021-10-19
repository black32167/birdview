package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentSnapshot
import org.birdview.BVProfiles
import org.birdview.model.BVDocumentFilter
import org.birdview.source.SourceType
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository


@Profile(BVProfiles.CLOUD)
@Repository
open class BVFireDocumentUnderlyingStorage(
    open val collectionAccessor: BVFireStoreAccessor,
) {
    class BVFirePersistingDocument (
        val id: String,
        val content:String, // Serialized BVDocument
        val sourceName: String,
        val sourceType: SourceType,
        val updated: Long,
        val indexed: Long,
        val bvUser: String,
        val externalIds: List<String>,
        val externalRefs: List<String>
    )

    fun updateDocument(persistent: BVFirePersistingDocument) {
        docCollection().document(persistent.id).set(persistent)
    }
    
    fun findDocuments(filter: BVDocumentFilter): List<DocumentSnapshot> =
        docCollection()
            .run {
                filter.updatedPeriod.after?.let { after ->
                    whereGreaterThan(BVFirePersistingDocument::updated.name, after.toInstant().toEpochMilli())
                } ?: this
            }
            .run {
                filter.updatedPeriod.before?.let { before ->
                    whereLessThan(BVFirePersistingDocument::updated.name, before.toInstant().toEpochMilli())
                } ?: this
            }
            .run {
                whereEqualTo(BVFirePersistingDocument::bvUser.name, filter.userFilter.userAlias)
            }
            .run {
                filter.sourceName
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { sourceName ->
                        whereEqualTo(BVFirePersistingDocument::sourceName.name, sourceName)
                    }
                    ?: this
            }
            .get().get()
            .documents

    fun getDocumentsByExternalIds(externalDocsIds: Collection<String>): List<DocumentSnapshot> =
        if (externalDocsIds.isEmpty()) {
            listOf()
        } else {
            docCollection()
                .whereArrayContainsAny(BVFirePersistingDocument::externalIds.name, externalDocsIds.toList())
                .get().get().documents
        }
    
    fun getReferringDocumentsByRefIds(externalDocsIds: Collection<String>): List<DocumentSnapshot> =
        if (externalDocsIds.isEmpty()) {
            listOf()
        } else {
            docCollection()
                .whereArrayContainsAny(BVFirePersistingDocument::externalRefs.name, externalDocsIds.toList())
                .get().get()
                .documents
        }

    private fun docCollection() = collectionAccessor.getDocumentsCollection()
}