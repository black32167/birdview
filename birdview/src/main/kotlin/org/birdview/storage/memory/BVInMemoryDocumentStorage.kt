package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.RelativeHierarchyType
import org.birdview.model.RelativeHierarchyType.*
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
                    externalId2SourceNames
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

    override fun getIncomingRefsByExternalIds(externalIds: Set<String>): List<BVDocumentRef> =
        externalIds.flatMap { externalId ->
            val refs: List<BVDocumentRef> = sourceName2SourceStorage.values.flatMap { source ->
                val refs: List<BVDocumentRef> = source.internalId2docHolder.values.map(DocHolder::doc)
                    .flatMap { parentCandidateDoc ->
                        parentCandidateDoc.refs.filter { ref ->
                            ref.docId.id == externalId
                        }.mapNotNull { originalRef->parentCandidateDoc.ids.firstOrNull()?.let { parentId->
                            val newRelType = if (originalRef.hierarchyType == LINK_TO_PARENT) {
                                LINK_TO_CHILD
                            } else if (originalRef.hierarchyType == LINK_TO_CHILD) {
                                LINK_TO_PARENT
                            } else {
                                UNSPECIFIED
                            }
                            BVDocumentRef(parentId, newRelType)
                        }}
                    }
                refs
            }.toList()
            refs
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