package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentStatus
import org.birdview.storage.BVDocumentStorage
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class BVInMemoryDocumentStorage(
        private val docPredicate: BVDocumentPredicate
): BVDocumentStorage {
    private val log = LoggerFactory.getLogger(BVInMemoryDocumentStorage::class.java)

    private class DocHolder (
            @Volatile
            var doc: BVDocument
    )
    private inner class SourceStorage (
        private val sourceName: String
    ) {
        val externalId2docHolder: MutableMap<String, DocHolder> = ConcurrentHashMap() // id -> doc
        val internalId2docHolder: MutableMap<String, DocHolder> = ConcurrentHashMap()

        fun findDocument(docId: String): BVDocument? = externalId2docHolder[docId]?.doc

        fun updateDocument(id: String, doc: BVDocument) {
            val docHolder = internalId2docHolder.computeIfAbsent(doc.internalId) {
                DocHolder(doc)
            }
            docHolder.doc = doc
            externalId2docHolder[id] = docHolder
        }

        fun findDocuments(filter: BVDocumentFilter): List<BVDocument> =
                externalId2docHolder.values
                        .map (DocHolder::doc)
                        .map (::prepareToFilter)
                        .filter { docPredicate.test(it, filter) }

        fun count() = internalId2docHolder.size
    }

    // sourceName -> SourceStorage
    private val name2SourceStorage = ConcurrentHashMap<String, MutableMap<String, SourceStorage>>()

    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> {
        return name2SourceStorage.values
                .flatMap { it.values }
                .flatMap { it.findDocuments(filter) }
                .toMutableList()
    }

    override fun getDocuments(searchingDocsIds: Set<String>): List<BVDocument> =
        searchingDocsIds.mapNotNull (this::findDocument)

    override fun updateDocument(doc: BVDocument) {
        doc.ids
                .map { it.id }
                .forEach { strId->
                    name2SourceStorage
                            .computeIfAbsent(strId) {
                                ConcurrentHashMap<String, SourceStorage>()
                            }
                            .computeIfAbsent(doc.sourceName, ::SourceStorage)
                            .updateDocument(strId, doc)
                }
    }

    override fun count(): Int =
            name2SourceStorage.values
                    .flatMap { it.values }
                    .map { it.count() }
                    .sum()


    private fun prepareToFilter(doc: BVDocument): BVDocument =
            if (doc.status == BVDocumentStatus.INHERITED) {
                doc.copy(status = inferDocStatus(doc))
            } else {
                doc
            }

    private fun findDocument(docId: String): BVDocument? =
            name2SourceStorage[docId]
                    ?.values
                    ?.mapNotNull { storage -> storage.findDocument(docId) }
                    ?.firstOrNull()

    private fun inferDocStatus(doc: BVDocument): BVDocumentStatus? {
//        if (doc.status == BVDocumentStatus.INHERITED) {
//            val docParent = getDocumentParent(doc)
//            return docParent?.status
//                    ?: inferDocStatusFromUpdatedTimestamp(doc)
//        }
        return doc.status
    }

    private fun inferDocStatusFromUpdatedTimestamp(doc:BVDocument): BVDocumentStatus? {
        val docInstant = doc.updated?.toInstant()
        return if (docInstant != null && docInstant > Instant.now().minus(7, ChronoUnit.DAYS)) {
            BVDocumentStatus.PROGRESS
        } else {
            BVDocumentStatus.BACKLOG
        }
    }

}