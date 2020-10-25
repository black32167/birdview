package org.birdview.user.document

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserSourceStorage
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVDocumentUtils.getReferencedDocIds
import org.birdview.utils.BVTimeUtil
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
class BVDocumentsLoader (
        sourceManagers: List<BVTaskSource>,
        private val documentStorage: BVDocumentStorage,
        private val userSourceStorage: BVUserSourceStorage,
        private val sourceSecretsStorage: BVSourceSecretsStorage
) {
    private val log = LoggerFactory.getLogger(BVDocumentsLoader::class.java)
    private val sourceManagersMap = sourceManagers.map { it.getType() to it }.toMap()
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory("BVTaskService"))

    fun loadDocuments(bvUser: String):List<Future<*>> =
            listEnabledSourceConfigs(bvUser)
                    .map { source ->
                        val subtaskFutures = mutableListOf<Future<*>>()
                        CompletableFuture.runAsync(Runnable {
                            BVTimeUtil.logTime("Loading data from ${source.sourceType}") {
                                val sourceManager = getSourceManager(source.sourceType)
                                sourceManager.getTasks(bvUser, TimeIntervalFilter(after = ZonedDateTime.now().minusMonths(1))) { docChunk ->
                                    docChunk.forEach { doc ->
                                        doc.ids.firstOrNull()?.also { id -> documentStorage.updateDocument(id, doc) }
                                    }

                                    subtaskFutures.add(executor.submit {
                                        loadReferredDocs(bvUser, docChunk) { docChunk ->
                                            docChunk.forEach { doc ->
                                                doc.ids.firstOrNull()?.also { id -> documentStorage.updateDocument(id, doc) }
                                            }
                                        }
                                    })
                                }
                                subtaskFutures.forEach { it.get() }
                            }
                        }, executor)
                    }

    private fun loadReferredDocs(bvUser: String, filteredDocs: List<BVDocument>, chunkConsumer: (List<BVDocument>) -> Unit) {
        val missedDocsIds = getReferencedDocIds(filteredDocs)
        loadDocsByIds(bvUser, missedDocsIds, chunkConsumer)
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


    private fun listEnabledSourceConfigs(bvUser:String):List<BVAbstractSourceConfig> =
            userSourceStorage.listUserSources(bvUser)
                    .filter { sourceName -> isEnabled(bvUser = bvUser, sourceName = sourceName) }
                    .mapNotNull (sourceSecretsStorage::getConfigByName)

    private fun isEnabled(bvUser: String, sourceName: String): Boolean = try {
        userSourceStorage.getSourceProfile(bvUser = bvUser, sourceName = sourceName).enabled
    } catch (exception: java.lang.Exception) {
        log.warn("Error reading source '${sourceName}' config for user '${bvUser}'")
        false
    }
}
