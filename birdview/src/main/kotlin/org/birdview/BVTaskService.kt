package org.birdview

import org.birdview.analysis.BVDocument
import org.birdview.request.TasksRequest
import org.birdview.source.BVTaskSource
import org.birdview.utils.BVConcurrentUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named


@Named
open class BVTaskService(
        private val groupDescriber: GroupDescriber,
        private var sources: List<BVTaskSource>
)  {
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory())

    @Cacheable("bv")
    open fun getTaskGroups(request: TasksRequest): List<BVDocument> {
        val filteredDocs:MutableList<BVDocument> = sources
                .filter { filterSource(it, request) }
                .map { source -> executor.submit(Callable<List<BVDocument>> { source.getTasks(request) }) }
                .mapNotNull { getSwallowException(it) }
                .flatten()
                .toMutableList()

        val allDocs = (filteredDocs + getReferredDocs(filteredDocs, request)).toMutableList()

        if (request.grouping) {
            linkDocs(allDocs)
            return allDocs
        } else {
            return removeParents(allDocs)
        }
    }

    @CacheEvict(value = ["bv"], allEntries = true)
    open fun invalidateCache() {
    }

    private fun removeParents(allDocs: List<BVDocument>): List<BVDocument> {
        val references = allDocs.flatMap { doc -> doc.refsIds + doc.groupIds.map { it.id } }.toSet()
        return allDocs.filter { doc -> !doc.ids.asSequence().map { it.id }.any { id-> references.contains(id) } }
    }

    private fun getReferredDocs(filteredDocs: MutableList<BVDocument>, request: TasksRequest): List<BVDocument> =
            if (request.grouping) {
                val materializedIds = filteredDocs.flatMap { it.ids }.map { it.id }
                val referencedIds = filteredDocs.flatMap { it.refsIds } + filteredDocs.flatMap { it.groupIds }.map { it.id }
                val missedDocsIds = referencedIds - materializedIds
                loadDocs(missedDocsIds)
            } else {
                listOf()
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
                        ?.let { ids -> try {
                            source.loadByIds(ids)
                        } catch (e:Exception) {
                            e.printStackTrace()
                            null
                        }}
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
                        if (parentDocs.isNotEmpty()) {
                            parentDocs.forEach{ it.subDocuments.add(doc) }
                            collectionsIterator.remove()
                        }
                    }
        }
    }

}