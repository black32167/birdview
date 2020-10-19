package org.birdview

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentOperationType
import org.birdview.model.*
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserSourceStorage
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVTimeUtil
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.ChronoZonedDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named
import kotlin.NoSuchElementException

@Named
open class BVTaskService(
        sourceManagers: List<BVTaskSource>,
        val userSourceStorage: BVUserSourceStorage,
        val sourceSecretsStorage: BVSourceSecretsStorage
) {
    private val sourceManagersMap = sourceManagers.map { it.getType() to it }.toMap()

    private val log = LoggerFactory.getLogger(BVTaskService::class.java)
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory("BVTaskService"))
    // id -> doc
    private val docsMap = ConcurrentHashMap<BVDocumentId, BVDocument>()
    private val usersRetrieved = ConcurrentHashMap<String, Boolean>()

    open fun getDocuments(filter: BVDocumentFilter): List<BVDocument> {
        loadAsync(filter.userFilter.userAlias)
        BVTimeUtil.printStats()

        val filteredDocs = BVTimeUtil.logTime("Filtering documents") {
            docsMap
                    .values
                    .filter { doc ->
                        filterDocument(doc, filter)
                    }
                    .toMutableList()
        }

        if (filter.sourceType != "") {
            return filteredDocs
        }

        val allDocs = filteredDocs + getReferencedDocs(filteredDocs)

        val linkedDocs = BVTimeUtil.logTime("Linking documents") {
            linkDocs(allDocs)
        }

        if (filter.representationType == RepresentationType.LIST) {
            return linkedDocs.map { it.copy(subDocuments = optimizeHierarchy(it.subDocuments)) }
        }
        return linkedDocs;
    }

    private fun optimizeHierarchy(docs: List<BVDocument>): MutableList<BVDocument> {
        return docs.flatMap { optimizeDoc(it) }.toMutableList()
    }

    private fun optimizeDoc(doc: BVDocument): Iterable<BVDocument> =
        if (doc.subDocuments.isEmpty()) {
            listOf(doc)
        } else if (doc.subDocuments.size < 3) {
            optimizeHierarchy(doc.subDocuments)
        } else {
            listOf(doc.copy(subDocuments = optimizeHierarchy(doc.subDocuments)))
        }

    private fun loadAsync(user: String) {
        usersRetrieved.computeIfAbsent(user ?: "") {
            loadDocuments(user).forEach{
                try {
                    it.get()
                } catch (e: java.lang.Exception) {
                    log.error("", e)
                }
            }
            true
        }
    }

    private fun listEnabledSourceConfigs(bvUser:String):List<BVAbstractSourceConfig> =
            userSourceStorage.listUserSources(bvUser)
                    .filter { sourceName -> isEnabled(bvUser = bvUser, sourceName = sourceName) }
                    .mapNotNull (sourceSecretsStorage::getConfigByName)

    open fun loadDocuments(bvUser: String):List<Future<*>> =
            listEnabledSourceConfigs(bvUser)
                    .map { source ->
                        val subtaskFutures = mutableListOf<Future<*>>()
                        CompletableFuture.runAsync(Runnable {
                            BVTimeUtil.logTime("Loading data from ${source.sourceType}") {
                                val sourceManager = getSourceManager(source.sourceType)
                                sourceManager.getTasks(bvUser, TimeIntervalFilter(after = ZonedDateTime.now().minusMonths(1))) { docChunk ->
                                    docChunk.forEach { doc ->
                                        doc.ids.firstOrNull()?.also { id -> docsMap[id] = doc }
                                    }

                                    subtaskFutures.add(executor.submit {
                                        loadReferredDocs(bvUser, docChunk) { docChunk ->
                                            docChunk.forEach { doc ->
                                                doc.ids.firstOrNull()?.also { id -> docsMap[id] = doc }
                                            }
                                        }
                                    })
                                }
                                subtaskFutures.forEach { it.get() }
                            }
                        }, executor)
                    }

    private fun isEnabled(bvUser: String, sourceName: String): Boolean = try {
        userSourceStorage.getSourceProfile(bvUser = bvUser, sourceName = sourceName).enabled
    } catch (exception: java.lang.Exception) {
        log.warn("Error reading source '${sourceName}' config for user '${bvUser}'")
        false
    }

    open fun invalidateCache() {
        docsMap.clear()
        usersRetrieved.clear()
    }

    open fun filterDocument(doc: BVDocument, filter: BVDocumentFilter): Boolean {
        if(filter.sourceType != "" && filter.sourceType?.let { filterSource -> doc.ids.any { it.sourceName == filterSource }} == false) {
            return false
        }

        val docUpdated = inferDocUpdated(doc, filter.userFilter)
        if (filter.updatedPeriod.after != null) {
            if (docUpdated == null || filter.updatedPeriod.after > docUpdated) {
                log.trace("Filtering out doc #{} (updatedPeriod.after)", doc.title)
                return false
            }
        }

        if (filter.updatedPeriod.before != null) {
            if (docUpdated == null || filter.updatedPeriod.before <= docUpdated) {
                log.trace("Filtering out doc #{} (updatedPeriod.before)", doc.title)
                return false
            }
        }

        val inferredDocStatus = inferDocStatus(doc)
        val targetDocumentStatuses = getTargetDocStatuses(filter.reportType)
        if (!targetDocumentStatuses.contains(inferredDocStatus)) {
            log.trace("Filtering out doc #{} (inferredDocStatus)", doc.title)
            return false
        }

        if (!targetDocumentStatuses.contains(doc.status)) {
            log.trace("Filtering out doc #{} (doc.status)", doc.title)
            return false
        }

        val userFilter = filter.userFilter
        val hasFilteredUser = doc.users.any { docUser ->
                val filteringUser = userSourceStorage.getSourceProfile(userFilter.userAlias, docUser.sourceName).sourceUserName
                filteringUser == docUser.userName && userFilter.role == docUser.role
        }
        if (!hasFilteredUser) {
            log.trace("Filtering out doc #{} (hasFilteredUser)", doc.title)
            return false
        }

        log.trace("Including doc #{}", doc.title)
        return true
    }

    open fun inferDocUpdated(doc: BVDocument, userFilter: UserFilter): ChronoZonedDateTime<*>? {
        val date = getLastOperationDate(doc, userFilter)
                ?: getDocDate(doc)

        return date?.toInstant()?.atZone(ZoneId.of("UTC"))
    }

    open fun getLastOperationDate(doc: BVDocument, userFilter: UserFilter): Date? =
            getLastOperation(doc, userFilter) ?.created

    private fun getLastOperation(doc: BVDocument, userFilter: UserFilter): BVDocumentOperation? {
        if (userFilter.role != UserRole.IMPLEMENTOR) {
            return null
        }
        return doc.lastOperations.firstOrNull { operation ->
            val filteringUser = userSourceStorage.getSourceProfile(userFilter.userAlias, operation.sourceName).sourceUserName
            filteringUser == operation.author && mapOperationTypeToRole(operation.type).contains(userFilter.role)
        }
    }

    private fun mapOperationTypeToRole(type: BVDocumentOperationType): Set<UserRole> =
        when (type) {
            BVDocumentOperationType.COLLABORATE -> setOf(UserRole.IMPLEMENTOR)
            else -> setOf(UserRole.WATCHER)
        }

    private fun getDocDate(doc: BVDocument): Date? =
            if(doc.closed != null && doc.closed < doc.updated) {
                doc.closed //doc.closed
            } else {
                doc.updated
            }

    private fun inferDocStatus(doc: BVDocument): BVDocumentStatus? {
        val parentStatuses = doc.refsIds
                .map { key -> getDocByStringKey(key)?.status }
        if (parentStatuses.isNotEmpty() && parentStatuses.all { it == BVDocumentStatus.DONE }) {
            return BVDocumentStatus.DONE
        }
        return doc.status
    }

    private fun getDocByStringKey(key: String): BVDocument? =
            docsMap.values.find { doc -> doc.ids.any { it.id == key } }

    private fun getTargetDocStatuses(reportType: ReportType) = when (reportType) {
        ReportType.WORKED -> listOf(BVDocumentStatus.DONE, BVDocumentStatus.PROGRESS)
        ReportType.PLANNED -> listOf(BVDocumentStatus.PROGRESS, BVDocumentStatus.PLANNED, BVDocumentStatus.BACKLOG)
    }

    // TODO: non-optimal
    private fun getReferencedDocs(filteredDocs: List<BVDocument>): List<BVDocument> {
        val missedDocsIds = getReferencedDocIds(filteredDocs)
        return docsMap.values.filter { docId -> docId.ids.find { missedDocsIds.contains(it.id) } != null }
    }

    private fun loadReferredDocs(bvUser: String, filteredDocs: List<BVDocument>, chunkConsumer: (List<BVDocument>) -> Unit) {
        val missedDocsIds = getReferencedDocIds(filteredDocs)
        loadDocsByIds(bvUser, missedDocsIds, chunkConsumer)
    }

    private fun getReferencedDocIds(filteredDocs: List<BVDocument>): Set<String> {
        val materializedIds = filteredDocs.flatMap { it.ids }.map { it.id }.toSet()
        val referencedIds = (filteredDocs.flatMap { it.refsIds } + filteredDocs.flatMap { it.groupIds }.map { it.id }).toSet()
        return referencedIds - materializedIds
    }

    private fun loadDocsByIds(bvUser: String, missedDocsIds: Set<String>, chunkConsumer: (List<BVDocument>) -> Unit) {
        val sourceType2SourceNames: Map<SourceType, List<String>> =
                listEnabledSourceConfigs(bvUser).groupBy ({ it.sourceType}, {it.sourceName})
        val type2Ids: Map<SourceType, List<String>> = missedDocsIds
                .fold(mutableMapOf<SourceType, MutableList<String>>()) { acc, id ->
                    getSourceTypes(id)?.let { type -> acc.computeIfAbsent(type) { mutableListOf() }.add(id) }
                    acc
                }

        return type2Ids.entries.forEach { (sourceType, sourceIds) ->
            val sourceNames = sourceType2SourceNames[sourceType]
            val sourceManager = getSourceManager(sourceType)
            sourceNames?.forEach { sourceName ->
                try {
                    sourceManager.loadByIds(sourceName, sourceIds, chunkConsumer)
                } catch (e: Exception) {
                    log.error("", e)
                }
            }
        }
    }

    private fun getSourceManager(sourceType: SourceType) =
            sourceManagersMap[sourceType] ?: throw NoSuchElementException("Unknown source type ${sourceType}")

    private fun getSourceTypes(id: String): SourceType? =
            sourceManagersMap.values.find { it.canHandleId(id) }?.getType()

    private fun linkDocs(_docs: List<BVDocument>): List<BVDocument> {
        //FIXME (subDocuments)
        val docs = _docs.map { it.copy(subDocuments = mutableListOf()) }.toMutableList()

        val groupId2Group = docs
                .flatMap { doc -> doc.ids.map { id -> id to doc } }
                .groupBy({ entry -> entry.first.id }, { entry -> entry.second })

        val collectionsIterator = docs.iterator()
        while (collectionsIterator.hasNext()) {
            collectionsIterator.next()
                    .also { doc: BVDocument ->
                        val parentDocs: List<BVDocument> = (doc.refsIds + doc.groupIds.map { it.id })
                                .flatMap { refId -> (groupId2Group[refId] ?: emptyList<BVDocument>()) }
                        if (parentDocs.isNotEmpty()) {
                            parentDocs.forEach { it.subDocuments.add(doc) }
                            collectionsIterator.remove()
                        }
                    }
        }
        return docs
    }
}