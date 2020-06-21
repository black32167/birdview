package org.birdview.api

import org.birdview.GroupDescriber
import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.request.TasksRequest
import org.birdview.source.BVTaskSource
import org.birdview.utils.BVConcurrentUtils
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
class BVTaskService(
        private val groupDescriber: GroupDescriber,
        private var sources: List<BVTaskSource>
)  {
    private val executor = Executors.newFixedThreadPool(3, BVConcurrentUtils.getDaemonThreadFactory())

    fun getTaskGroups(request: TasksRequest): List<BVDocument> {
        val docs:MutableList<BVDocument> = sources
                .filter { filterSource(it, request) }
                .map { source -> executor.submit(Callable<List<BVDocument>> { source.getTasks(request) }) }
                .map { getSwallowException(it) }
                .filterNotNull()
                .flatten()
                .toMutableList()

        linkDocs(docs)

        // Collect all groupIds
        val groupedDocumentsMap = docs.fold(mutableMapOf<BVDocumentId, MutableList<BVDocument>>()) { acc, doc ->
            doc.groupIds.forEach { groupId ->
                acc.computeIfAbsent(groupId) { mutableListOf() }.add(doc)
            }
            acc
        }

        val groupedDocsIds = groupedDocumentsMap.values
                .flatten()
                .flatMap { it.ids }
                .toSet()

        // Remove grouped documents
        val orphanedDocs = docs.filter { doc -> doc.ids.none(groupedDocsIds::contains) }

        val groupedDocuments:List<BVDocument> = groupedDocumentsMap
                .map { (groupDocId, collection) -> newGroupDoc(groupDocId, collection) }
                .sortedByDescending { it.getLastUpdated() } +
                listOf(newGroupDoc(null, orphanedDocs).apply { title = "--- Ungrouped items ---" })

        groupDescriber.describe(groupedDocuments)

        return groupedDocuments
    }

    private fun filterSource(source: BVTaskSource, request: TasksRequest) =
            request.sourceType ?.let { it == source.getType() }
                    ?: true

    private fun <T> getSwallowException(future: Future<T>): T? {
        try {
            return future.get()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun newGroupDoc(groupDocId: BVDocumentId?, collection: List<BVDocument>):BVDocument =
            BVDocument(
                    ids = groupDocId?.let { setOf(it) } ?: emptySet(),
                    subDocuments = collection.toMutableList())

    private fun linkDocs(docs: MutableList<BVDocument>) {
        val groupId2Group = docs
                .flatMap { collection -> collection.ids.map { it to collection } }
                .groupBy ({ entry -> entry.first.id }, { entry -> entry.second })

        val collectionsIterator = docs.iterator()
        while (collectionsIterator.hasNext()) {
            collectionsIterator.next()
                    .also { doc:BVDocument ->
                        val targetDocs:List<BVDocument> = doc.refsIds
                                .flatMap { refId -> (groupId2Group[refId] ?: emptyList<BVDocument>()) }
                        if (!targetDocs.isEmpty()) {
                            targetDocs.forEach{ it.subDocuments.add(doc) }
                            collectionsIterator.remove()
                        }
                    }
        }
    }

}