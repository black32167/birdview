package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentSnapshot
import org.birdview.BVProfiles
import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.memory.BVDocumentPredicate
import org.birdview.time.RealTimeService
import org.birdview.utils.JsonDeserializer
import org.springframework.context.annotation.Profile
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Named

@Profile(BVProfiles.CLOUD)
@Named
open class BVFireDocumentStorage(
    open val underlyingStorage: BVFireDocumentUnderlyingStorage,
    open val serializer: JsonDeserializer,
    open val timeService: RealTimeService,
    private val docPredicate: BVDocumentPredicate,
    private val userSourceConfigStorage: BVUserSourceConfigStorage
): BVDocumentStorage {
    companion object {
        private const val FIRESTORE_MAX_CHUNK_SIZE = 10
    }

    override fun getLastUpdatedDocument(bvUser: String, sourceName: String): OffsetDateTime? =
        underlyingStorage.getLatestDocument(bvUser, sourceName)
            ?.let { timstampMs -> Instant.ofEpochMilli(timstampMs).atOffset(ZoneOffset.UTC) }

    // TODO: add exception interceptor into spring-web
    // TODO: rework/decompose filter @Cacheable(BVCacheNames.DOCUMENTS_CACHE)
    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> =
        underlyingStorage.findDocuments(filter)
            .mapNotNull { docSnapshot -> extractDoc(docSnapshot) }
            .filter { docPredicate.test(it, filter) }

    override fun getDocuments(externalDocsIds: Collection<String>): List<BVDocument> =
        externalDocsIds.chunked(FIRESTORE_MAX_CHUNK_SIZE)
            .flatMap { referredDocsIdsChunk -> underlyingStorage.getDocumentsByExternalIds(referredDocsIdsChunk) }
            .mapNotNull { docSnapshot -> extractDoc(docSnapshot) }

    override fun updateDocument(doc: BVDocument, bvUser: String) {
        val persistent = BVFirePersistingDocument(
            id = doc.internalId,
            content = serializer.serializeToString(doc),
            updated = inferUpdated(doc),
            bvUser = bvUser,
            indexed = timeService.getNow().toInstant().toEpochMilli(),
            sourceName = doc.sourceName,
            sourceType = userSourceConfigStorage.getSource(bvUser = bvUser, sourceName = doc.sourceName)?.sourceType
                ?: throw NoSuchElementException("Source not found for bvUser = ${bvUser}, sourceName = ${doc.sourceName}",),
            externalIds = doc.ids.map { it.id },
            externalRefs = doc.refs.map { it.docId.id }
        )
        underlyingStorage.updateDocument(persistent)
    }

    override fun count(): Int {
        return -1
    }

    override fun removeExistingExternalIds(externalIds: List<String>): List<String> {
        if (externalIds.isEmpty()) {
            return listOf()
        }

        val existingIds: List<String> =
            externalIds.chunked(FIRESTORE_MAX_CHUNK_SIZE)
                .flatMap { referredDocsIdsChunk -> underlyingStorage.getReferringDocumentsByRefIds(referredDocsIdsChunk) }
                .flatMap { docSnapshot -> docSnapshot.get(BVFirePersistingDocument::externalIds.name) as List<String> }

        return externalIds.filter { !existingIds.contains(it) }
    }

    override fun getReferringDocuments(externalIds: Set<String>): List<BVDocument> =
        externalIds.chunked(FIRESTORE_MAX_CHUNK_SIZE)
            .flatMap { referredDocsIdsChunk -> underlyingStorage.getReferringDocumentsByRefIds(referredDocsIdsChunk) }
            .mapNotNull { referringDocRef ->
                DocumentObjectMapper.toObjectCatching(referringDocRef, BVFirePersistingDocument::class)
            }
            .map { persistentDocContainer ->
                serializer.deserializeString(persistentDocContainer.content, BVDocument::class.java)
            }

    private fun extractDoc(docSnapshot: DocumentSnapshot): BVDocument? =
        DocumentObjectMapper.toObjectCatching(docSnapshot, BVFirePersistingDocument::class)
            ?.let { persistentDoc -> serializer.deserializeString(persistentDoc.content, BVDocument::class.java) }

    private fun inferUpdated(doc: BVDocument): Long =
        doc.operations
            .mapNotNull { it.created }
            .map { it.toInstant().toEpochMilli()  }
            .maxByOrNull { it }
            ?: doc.updated?.toInstant()?.toEpochMilli()
            ?: 0L
}