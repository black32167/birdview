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
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    fun getTaskGroups(request: TasksRequest): List<BVDocument> {
        val filteredDocs:MutableList<BVDocument> = sources
                .filter { filterSource(it, request) }
                .map { source -> executor.submit(Callable<List<BVDocument>> { source.getTasks(request) }) }
                .map { getSwallowException(it) }
                .filterNotNull()
                .flatten()
                .toMutableList()
        val materializedIds = filteredDocs.flatMap { it.ids }.map { it.id }
        val referencedIds = filteredDocs.flatMap { it.refsIds }
        val missedDocsIds = referencedIds - materializedIds
        val referredDocs = loadDocs(missedDocsIds)
        val allDocs = (filteredDocs + referredDocs).toMutableList()

        linkDocs(allDocs)

        return allDocs
    }

    private fun loadDocs(missedDocsIds: List<String>): List<BVDocument> {
        val type2Ids:Map<String, List<String>> = missedDocsIds
                .fold(mutableMapOf<String, MutableList<String>>()) { acc, id ->
                    getSourceTypes(id)?.let { type -> acc.computeIfAbsent(type) { mutableListOf() }.add(id) }
                    acc
                }
        return sources
                .filter { source -> type2Ids.contains(source.getType()) }
                .flatMap { source ->
                    type2Ids[source.getType()]
                        ?.let { ids -> source.loadByIds(ids) }
                        ?: emptyList()
                }
    }

    private fun getSourceTypes(id: String): String? =
        sources.find{ it.canHadleId(id) } ?.getType()

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
                .flatMap { doc -> doc.ids.map { id -> id to doc } }
                .groupBy ({ entry -> entry.first.id }, { entry -> entry.second })

        val collectionsIterator = docs.iterator()
        while (collectionsIterator.hasNext()) {
            collectionsIterator.next()
                    .also { doc:BVDocument ->
                        val parentDocs:List<BVDocument> = (doc.refsIds + doc.groupIds.map { it.id })
                                .flatMap { refId -> (groupId2Group[refId] ?: emptyList<BVDocument>()) }
                        if (!parentDocs.isEmpty()) {
                            parentDocs.forEach{ it.subDocuments.add(doc) }
                            collectionsIterator.remove()
                        }
                    }
        }
    }

}