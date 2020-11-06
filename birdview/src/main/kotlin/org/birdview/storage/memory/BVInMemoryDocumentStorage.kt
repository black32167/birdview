package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentStatus
import org.birdview.source.BVDocumentsRelation
import org.birdview.storage.BVDocumentStorage
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class BVInMemoryDocumentStorage(
        private val docPredicate: BVDocumentPredicate
): BVDocumentStorage {
    // id -> doc
    private val docsMap = ConcurrentHashMap<BVDocumentId, BVDocument>()

    override fun findDocuments(filter: BVDocumentFilter): List<BVDocument> {
        return docsMap
                .values
                .map { prepareToFilter(it) }
                .filter { docPredicate.test(it, filter) }
                .toMutableList()
    }

    override fun getDocuments(searchingDocsIds: Set<String>): List<BVDocument> =
        docsMap.values.filter { docId -> docId.ids.find { searchingDocsIds.contains(it.id) } != null }

    override fun updateDocument(id: BVDocumentId, doc: BVDocument) {
        docsMap[id] = doc
    }

    private fun prepareToFilter(doc: BVDocument) =
            if (doc.status == BVDocumentStatus.INHERITED) {
                doc.copy(status = inferDocStatus(doc))
            } else {
                doc
            }

    override fun getDocumentParent(doc: BVDocument): BVDocument? =
        doc.refsIds
                .mapNotNull { refId:String -> docsMap[refId] }
                .firstOrNull { candidteParent -> isParent(candidteParent, doc) }

    private fun isParent(candidateParent: BVDocument, doc: BVDocument) =
        BVDocumentsRelation.from(candidateParent, doc)?.parent === candidateParent

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