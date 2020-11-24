package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentStatus
import org.birdview.source.BVDocumentsRelation
import org.birdview.source.SourceType
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVSourcesManager
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class BVInMemoryDocumentStorage(
        private val docPredicate: BVDocumentPredicate,
        private val sourceManager: BVSourcesManager
): BVDocumentStorage {
    private val log = LoggerFactory.getLogger(BVInMemoryDocumentStorage::class.java)

    inner class SourcesBundle (
        private val sourceType: SourceType
    ) {
        private val sourceStoragesMap: MutableMap<String, SourceStorage> = ConcurrentHashMap()

        fun findDocument(docId: String): BVDocument? = sourceStoragesMap.values
                .mapNotNull { storage -> storage.findDocument(docId) }
                .firstOrNull()

        fun updateDocument(id: String, doc: BVDocument) {
            sourceStoragesMap.computeIfAbsent(doc.sourceName, ::SourceStorage)
                    .updateDocument(id, doc)
        }

        fun findDocuments(filter: BVDocumentFilter): List<BVDocument> =
            sourceStoragesMap.values
                    .flatMap { it.findDocuments(filter) }
    }

    inner class SourceStorage (
        private val sourceName: String
    ) {
        val docsMap: MutableMap<String, BVDocument> = ConcurrentHashMap() // id -> doc
        fun findDocument(docId: String): BVDocument? = docsMap[docId]
        fun updateDocument(id: String, doc: BVDocument) {
            docsMap[id] = doc
        }

        fun findDocuments(filter: BVDocumentFilter): List<BVDocument> =
                docsMap.values
                        .map { prepareToFilter(it) }
                        .filter { docPredicate.test(it, filter) }
    }

    // sourceName -> SourceStorage
    private val sourceBundlesMap = ConcurrentHashMap<SourceType, SourcesBundle>()

    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> {
        return sourceBundlesMap.values
                .flatMap { it.findDocuments(filter) }
                .toMutableList()
    }

    override fun getDocuments(searchingDocsIds: Set<String>): List<BVDocument> =
        searchingDocsIds.mapNotNull (this::findDocument)

    override fun updateDocument(id: String, doc: BVDocument) {
        val sourceName = doc.sourceName
        val sourceType:SourceType = sourceManager.getSourceType(sourceName)
                ?: throw IllegalArgumentException("Can't find source type by name '${sourceName}', can't update document ${id}")

        val bundle = sourceBundlesMap.computeIfAbsent(sourceType) { SourcesBundle(it) }
        bundle.updateDocument(id, doc)
    }

    private fun prepareToFilter(doc: BVDocument): BVDocument =
            if (doc.status == BVDocumentStatus.INHERITED) {
                doc.copy(status = inferDocStatus(doc))
            } else {
                doc
            }

    override fun getDocumentParent(doc: BVDocument): BVDocument? =
        doc.refs
                .mapNotNull { ref -> findDocument(ref.docId.id) }
                .firstOrNull { candidateParent -> isParent(candidateParent, doc) }

    private fun findDocument(docId: String) = sourceBundlesMap.values.asSequence()
            .mapNotNull { bundle -> bundle.findDocument(docId) }
            .firstOrNull()

    private fun isParent(candidateParent: BVDocument, doc: BVDocument) =
            doc.refs
                    .filter { rel -> candidateParent.ids.any { parentId-> parentId == rel.docId} }
                    .any { rel->
                        BVDocumentsRelation.from(candidateParent, doc, rel)?.parent === candidateParent
                    }


    private fun inferDocStatus(doc: BVDocument): BVDocumentStatus? {
        if (doc.status == BVDocumentStatus.INHERITED) {
            val docParent = getDocumentParent(doc)
            return docParent?.status
                    ?: inferDocStatusFromUpdatedTimestamp(doc)
        }
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