package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentStatus
import org.birdview.storage.BVDocumentStorage
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
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

        fun findDocumentByExternalId(id: String): BVDocument? =
                externalId2docHolder[id]?.doc

        fun count() = internalId2docHolder.size
    }

    // docId -> sourceName -> SourceStorage
    private val id2SourceNames = ConcurrentHashMap<String, MutableSet<String>>()
    private val sourceName2SourceStorage = ConcurrentHashMap<String, SourceStorage>()

    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> {
        return sourceName2SourceStorage.values
                .flatMap { it.findDocuments(filter) }
                .toMutableList()
    }

    override fun getDocuments(searchingDocsIds: Set<String>): List<BVDocument> =
        searchingDocsIds.mapNotNull (this::findDocument)

    override fun updateDocument(doc: BVDocument) {
        doc.ids
                .map { it.id }
                .forEach { docExternalId->
                    id2SourceNames
                        .computeIfAbsent(docExternalId) { Collections.newSetFromMap(ConcurrentHashMap()) }
                        .add(doc.sourceName)
                    sourceName2SourceStorage
                        .computeIfAbsent(doc.sourceName, ::SourceStorage)
                        .updateDocument(docExternalId, doc)
                }
    }

    override fun count(): Int =
        sourceName2SourceStorage.values
            .map { it.count() }
            .sum()

    override fun containsDocWithExternalId(externalId: String): Boolean =
        sourceName2SourceStorage.values
            .any { sourceStorage -> sourceStorage.findDocumentByExternalId(externalId) != null }


    private fun prepareToFilter(doc: BVDocument): BVDocument =
            if (doc.status == BVDocumentStatus.INHERITED) {
                doc.copy(status = inferDocStatus(doc))
            } else {
                doc
            }

    private fun findDocument(externalDocId: String): BVDocument? =
        id2SourceNames[externalDocId]
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