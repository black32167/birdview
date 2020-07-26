package org.birdview

import org.apache.commons.logging.LogFactory
import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.model.BVDocumentFilter
import org.birdview.model.BVDocumentStatus
import org.birdview.model.ReportType
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVTaskSource
import org.birdview.utils.BVConcurrentUtils
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import javax.inject.Named

@Named
open class BVTaskService(
        private var sources: List<BVTaskSource>
)  {
    private val log = LogFactory.getLog(BVTaskService::class.java)
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory("BVTaskService"))
    // id -> doc
    private val docsMap = ConcurrentHashMap<BVDocumentId, BVDocument>()
    private val usersRetrieved = ConcurrentHashMap<String, Boolean>()

  //  @Cacheable("bv")
    open fun getDocuments(filter: BVDocumentFilter): List<BVDocument> {
        filter.userFilters.map {it.userAlias}.distinct().forEach { user ->
          loadAsync(user)
        }

        val filteredDocs = docsMap
                .values
                .filter { doc -> filterDocument(doc, filter) }
                .toMutableList()

        val allDocs = filteredDocs + getReferencedDocs(filteredDocs)

        return linkDocs(allDocs)
    }

    private fun loadAsync(user:String?) {
        usersRetrieved.computeIfAbsent(user ?: "") {
            loadDocuments(user)
            true
        }
    }

    open fun loadDocuments(user: String?) {
        sources
             //   .filter { filterSource(it, request) }
             //   .filter { filterSource(it, "trello") } //!!!
                .forEach { source ->
                    executor.submit {
                        source.getTasks(user, TimeIntervalFilter(after = ZonedDateTime.now().minusMonths(1))) { docChunk->
                            docChunk.forEach { doc ->
                                doc.ids.firstOrNull()?.also{ id -> docsMap[id] = doc }
                            }
                            loadReferredDocs(docChunk) { docChunk ->
                                docChunk.forEach { doc ->
                                    doc.ids.firstOrNull()?.also { id -> docsMap[id] = doc }
                                }
                            }
                        }
                    }
                }
    }

   // @CacheEvict(value = ["bv"], allEntries = true)
    open fun invalidateCache() {
       docsMap.clear()
       usersRetrieved.clear()
    }

    private fun filterDocument(doc:BVDocument, request: BVDocumentFilter) : Boolean {
        val docCreated = doc.created?.toInstant()?.atZone(ZoneId.of("UTC"))
        if (request.updatedPeriod.after != null) {
            if(docCreated == null || request.updatedPeriod.after > docCreated) {
                return false
            }
        }

        if (request.updatedPeriod.before != null) {
            if(docCreated == null || request.updatedPeriod.before <= docCreated) {
                return false
            }
        }

        var documentStatuses = getDocStatuses(request.reportType)
        if (documentStatuses.contains(doc.status)) {
            return false
        }
        return true
    }

    private fun getDocStatuses(reportType: ReportType) = when (reportType) {
        ReportType.LAST_DAY -> listOf(BVDocumentStatus.DONE, BVDocumentStatus.PROGRESS)
        ReportType.PLANNED -> listOf(BVDocumentStatus.PROGRESS, BVDocumentStatus.PLANNED, BVDocumentStatus.BACKLOG)
        ReportType.WORKED -> listOf(BVDocumentStatus.PROGRESS, BVDocumentStatus.PLANNED)
    }

    private fun removeParents(allDocs: List<BVDocument>, chunkConsumer: (List<BVDocument>) -> Unit): List<BVDocument> {
        val references = allDocs.flatMap { doc -> doc.refsIds + doc.groupIds.map { it.id } }.toSet()
        return allDocs.filter { doc -> !doc.ids.asSequence().map { it.id }.any { id-> references.contains(id) } }
    }

    // TODO: non-optimal
    private fun getReferencedDocs(filteredDocs: List<BVDocument>): List<BVDocument> {
        val missedDocsIds = getReferencedDocIds(filteredDocs)
        return docsMap.values.filter { docId -> docId.ids.find { missedDocsIds.contains(it.id) } != null }
    }

    private fun loadReferredDocs(filteredDocs: List<BVDocument>, chunkConsumer: (List<BVDocument>) -> Unit) {
        val missedDocsIds = getReferencedDocIds(filteredDocs)
        loadDocs(missedDocsIds, chunkConsumer)
    }

    private fun getReferencedDocIds(filteredDocs: List<BVDocument>): Set<String> {
        val materializedIds = filteredDocs.flatMap { it.ids }.map { it.id }.toSet()
        val referencedIds = (filteredDocs.flatMap { it.refsIds } + filteredDocs.flatMap { it.groupIds }.map { it.id }).toSet()
        return referencedIds - materializedIds
    }

    private fun loadDocs(missedDocsIds: Set<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
            val type2Ids:Map<String, List<String>> = missedDocsIds
                .fold(mutableMapOf<String, MutableList<String>>()) { acc, id ->
                    getSourceTypes(id)?.let { type -> acc.computeIfAbsent(type) { mutableListOf() }.add(id) }
                    acc
                }
        return sources
                .filter { source -> type2Ids.contains(source.getType()) }
                .forEach { source ->
                    type2Ids[source.getType()]
                        ?.also { ids ->
                            source.loadByIds(ids, chunkConsumer)
                        }
                }
    }

    private fun getSourceTypes(id: String): String? =
        sources.find{ it.canHandleId(id) } ?.getType()

    private fun linkDocs(_docs: List<BVDocument>):List<BVDocument> {
        //FIXME (subDocuments)
        val docs = _docs.map { it.copy(subDocuments = mutableListOf()) }.toMutableList()

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
        return docs
    }
}