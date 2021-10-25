package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Query
import org.birdview.BVProfiles
import org.birdview.model.BVDocumentFilter
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository


@Profile(BVProfiles.CLOUD)
@Repository
open class BVFireDocumentUnderlyingStorageImpl(
    private val collectionAccessor: BVFireStoreAccessor,
):BVFireDocumentUnderlyingStorage {

    override fun updateDocument(persistent: BVFirePersistingDocument) {
        docCollection(persistent.bvUser).document(persistent.id).set(persistent)
    }
    
    override fun findDocuments(filter: BVDocumentFilter): List<DocumentSnapshot> =
        docCollection(filter.userFilter.userAlias)
            .run {
                whereGreaterThan(
                        BVFirePersistingDocument::updated.name, filter.updatedPeriod.after.toInstant().toEpochMilli())
            }
            .run {
                filter.updatedPeriod.before?.let { before ->
                    whereLessThan(BVFirePersistingDocument::updated.name, before.toInstant().toEpochMilli())
                } ?: this
            }
//            .run {
//                whereEqualTo(BVFirePersistingDocument::bvUser.name, filter.userFilter.userAlias)
//            }
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

    override fun getDocumentsByExternalIds(bvUser:String, externalDocsIds: Collection<String>): List<DocumentSnapshot> =
        if (externalDocsIds.isEmpty()) {
            listOf()
        } else {
            docCollection(bvUser)
                .whereArrayContainsAny(BVFirePersistingDocument::externalIds.name, externalDocsIds.toList())
                .get().get().documents
        }
    
    override fun getReferringDocumentsByRefIds(bvUser:String, externalDocsIds: Collection<String>): List<DocumentSnapshot> =
        if (externalDocsIds.isEmpty()) {
            listOf()
        } else {
            docCollection(bvUser)
                .whereArrayContainsAny(BVFirePersistingDocument::externalRefs.name, externalDocsIds.toList())
                .get().get()
                .documents
        }

    override fun getLatestDocument(bvUser: String, sourceName: String): Long? =
        docCollection(bvUser)
            .select(BVFirePersistingDocument::updated.name)
//            .whereEqualTo(BVFirePersistingDocument::bvUser.name, bvUser)
            .whereEqualTo(BVFirePersistingDocument::sourceName.name, sourceName)
            .orderBy(BVFirePersistingDocument::updated.name, Query.Direction.DESCENDING)
            .limit(1)
            .get().get()
            .firstOrNull()
            ?.get(BVFirePersistingDocument::updated.name, Long::class.java)

    private fun docCollection(bvUser:String) =
        collectionAccessor.getDocumentsCollection(bvUser)
}