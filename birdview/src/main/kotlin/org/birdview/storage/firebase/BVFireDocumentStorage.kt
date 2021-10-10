package org.birdview.storage.firebase

import org.birdview.BVProfiles
import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentRef
import org.birdview.source.SourceType
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.memory.BVDocumentPredicate
import org.birdview.time.RealTimeService
import org.birdview.utils.JsonDeserializer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Profile(BVProfiles.CLOUD)
@Repository
open class BVFireDocumentStorage(
    open val collectionAccessor: BVFireStoreAccessor,
    open val serializer: JsonDeserializer,
    open val timeService: RealTimeService,
    private val docPredicate: BVDocumentPredicate,
    private val userSourceConfigStorage: BVUserSourceConfigStorage
): BVDocumentStorage {
    class BVFirePersistingDocument (
        val id: String,
        val content:String, // Serialized BVDocument
        val sourceName: String,
        val sourceType: SourceType,
        val updated: Long,
        val indexed: Long,
        val bvUser: String
    )

    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> =
        collectionAccessor.getDocumentsCollection()
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
                filter.sourceName?.let { sourceName ->
                    whereEqualTo(BVFirePersistingDocument::sourceName.name, sourceName)
                } ?: this
            }
            .get().get()
            .documents
            .mapNotNull { docSnapshot -> DocumentObjectMapper.toObjectCatching(docSnapshot, BVFirePersistingDocument::class) }
            .map { persistentDoc -> serializer.deserializeString(persistentDoc.content, BVDocument::class.java) }
            .filter { docPredicate.test(it, filter) }

    override fun getDocuments(searchingDocsIds: Collection<String>): List<BVDocument> {
        TODO("Not yet implemented")
    }

    override fun updateDocument(doc: BVDocument, bvUser: String) {
        val persistent = BVFirePersistingDocument(
            id = doc.internalId,
            content = serializer.serializeToString(doc),
            updated = inferUpdated(doc),
            bvUser = bvUser,
            indexed = timeService.getNow().toInstant().toEpochMilli(),
            sourceName = doc.sourceName,
            sourceType = userSourceConfigStorage.getSource(bvUser = bvUser, sourceName = doc.sourceName)?.sourceType
                ?: throw NoSuchElementException("Source not found for bvUser = ${bvUser}, sourceName = ${doc.sourceName}")
        )
        collectionAccessor.getDocumentsCollection().document(doc.internalId).set(persistent)
    }

    override fun count(): Int {
        TODO("Not yet implemented")
    }

    override fun containsDocWithExternalId(externalId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getIncomingRefsByExternalIds(externalIds: Set<String>): List<BVDocumentRef> {
        TODO("Not yet implemented")
    }

    private fun inferContributors(doc: BVDocument): List<String> =
        doc.operations.map { it.author }

    private fun inferUpdated(doc: BVDocument): Long =
        doc.operations
            .mapNotNull { it.created }
            .map { it.toInstant().toEpochMilli()  }
            .maxByOrNull { it }
            ?: doc.updated?.toInstant()?.toEpochMilli()
            ?: 0L
}