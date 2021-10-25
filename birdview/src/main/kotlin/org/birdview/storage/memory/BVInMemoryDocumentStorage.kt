package org.birdview.storage.memory

import org.birdview.BVProfiles
import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentStatus
import org.birdview.storage.BVDocumentStorage
import org.springframework.context.annotation.Profile
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
@Profile(BVProfiles.LOCAL)
class BVInMemoryDocumentStorage(
        private val docPredicate: BVDocumentPredicate
): BVDocumentStorage {
    private class DocHolder (
            @Volatile
            var doc: BVDocument
    )
    private inner class SourceStorage (
        private val sourceName: String
    ) {
        val externalId2docHolder: MutableMap<String, DocHolder> = ConcurrentHashMap() // id -> doc
        val internalId2docHolder: MutableMap<String, DocHolder> = ConcurrentHashMap()

        fun getAllDocuments() =
            externalId2docHolder.values.map { it.doc }

        fun findDocument(externalId: String): BVDocument? = externalId2docHolder[externalId]?.doc

        fun updateDocument(externalId: String, doc: BVDocument) {
            val existingDocHolder = externalId2docHolder.computeIfAbsent(externalId) {
                DocHolder(doc)
            }
            val internalId = existingDocHolder.doc.internalId

            existingDocHolder.doc = doc.copy(internalId = internalId)

            internalId2docHolder[internalId] = existingDocHolder
        }

        fun findDocuments(filter: BVDocumentFilter): List<BVDocument> =
            internalId2docHolder.values
                        .map (DocHolder::doc)
                        .map (::prepareToFilter)
                        .filter { docPredicate.test(it, filter) }

        fun findDocumentByExternalId(id: String): BVDocument? =
                externalId2docHolder[id]?.doc

        fun count() = internalId2docHolder.size
    }

    // docId -> sourceName -> SourceStorage
    private val externalId2SourceNames = ConcurrentHashMap<String, MutableSet<String>>()
    private val sourceName2SourceStorage = ConcurrentHashMap<String, SourceStorage>()

    override fun getLastUpdatedDocument(bvUser: String, sourceName: String): OffsetDateTime? =
        sourceName2SourceStorage.values
            .flatMap { it.getAllDocuments() }
            .filter { it.sourceName == sourceName } // No user check for local run
            .mapNotNull { it.updated  }
            .maxOrNull()

    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> {
        return sourceName2SourceStorage.values
                .flatMap { it.findDocuments(filter) }
                .toMutableList()
    }

    override fun getDocuments(bvUser: String, externalDocsIds: Collection<String>): List<BVDocument> =
        externalDocsIds.mapNotNull (this::findDocument)

    override fun updateDocument(bvUser: String, doc: BVDocument) {
        doc.ids
                .map { it.id }
                .forEach { docExternalId->
                    externalId2SourceNames
                        .computeIfAbsent(docExternalId) { Collections.newSetFromMap(ConcurrentHashMap()) }
                        .add(doc.sourceName)
                    sourceName2SourceStorage
                        .computeIfAbsent(doc.sourceName, ::SourceStorage)
                        .updateDocument(docExternalId, doc)
                }
    }

    override fun removeExistingExternalIds(bvUser: String, externalIds: List<String>): List<String> =
        externalIds.filter { externalId ->
            sourceName2SourceStorage.values
                .any { sourceStorage -> sourceStorage.findDocumentByExternalId(externalId) != null }
        }

    override fun getReferringDocuments(bvUser: String, externalIds: Set<String>): List<BVDocument> =
        externalIds.flatMap { externalId ->
            sourceName2SourceStorage.values.flatMap { source ->
                source.internalId2docHolder.values.map(DocHolder::doc)
            }.toList()
        }

    private fun prepareToFilter(doc: BVDocument): BVDocument =
            if (doc.status == BVDocumentStatus.INHERITED) {
                doc.copy(status = inferDocStatus(doc))
            } else {
                doc
            }

    private fun findDocument(externalDocId: String): BVDocument? =
        externalId2SourceNames[externalDocId]
            ?.asSequence()
            ?.map { sourceName ->
                sourceName2SourceStorage[sourceName]
                    ?.findDocument(externalDocId)
            }?.firstOrNull()


    private fun inferDocStatus(doc: BVDocument): BVDocumentStatus? {
//        if (doc.status == BVDocumentStatus.INHERITED) {
//            val docParent = getDocumentParent(doc)
//            return docParent?.status
//                    ?: inferDocStatusFromUpdatedTimestamp(doc)
//        }
        return doc.status
    }
}