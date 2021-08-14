package org.birdview.user.document

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.SourceType
import org.birdview.storage.BVDocumentProvidersManager
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVTimeUtil
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
class BVDocumentsLoader (
    private val sourcesManager: BVDocumentProvidersManager,
    private val sourceConfigProvider: BVSourceConfigProvider
) {
    private val log = LoggerFactory.getLogger(BVDocumentsLoader::class.java)
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory("DocLoader-%d"))

    fun loadDocuments(
        bvUser: String,
        timeIntervalFilter: TimeIntervalFilter,
        documentConsumer: BVSessionDocumentConsumer
    ): List<Future<*>> =
        sourceConfigProvider.listEnabledSourceConfigs(bvUser)
            .map { sourceConfig ->
                val sourceManager = sourcesManager.getBySourceType(sourceConfig.sourceType)
                CompletableFuture.runAsync(Runnable {
                    log.info("Loading data from ${sourceConfig.sourceType} for ${bvUser}...")
                    BVTimeUtil.logTimeAndReturn("Loading data from ${sourceConfig.sourceType} for ${bvUser}") {
                        try {
                            sourceManager.getTasks(
                                bvUser,
                                timeIntervalFilter,
                                sourceConfig,
                                documentConsumer
                            )
                        } catch (e: Throwable) {
                            log.error("Error loading documents for ${sourceConfig.sourceType}", e)
                        }
                    }
                }, executor)
            }

    fun loadDocsByIds(
        bvUser: String,
        missedDocsIds: Collection<String>,
        chunkConsumer: (List<BVDocument>) -> Unit
    ): List<Future<*>> {
        val subtaskFutures = mutableListOf<Future<*>>()
        val sourceType2SourceNames: Map<SourceType, List<String>> =
            sourceConfigProvider.listEnabledSourceConfigs(bvUser)
                .groupBy(BVSourceConfigProvider.SyntheticSourceConfig::sourceType, BVSourceConfigProvider.SyntheticSourceConfig::sourceName)
        val type2Ids: Map<SourceType, List<String>> = missedDocsIds
            .fold(mutableMapOf<SourceType, MutableList<String>>()) { acc, id ->
                sourcesManager.guessSourceTypesByDocumentId(id)
                    ?.let { type -> acc.computeIfAbsent(type) { mutableListOf() }.add(id) }
                acc
            }

        type2Ids.entries.forEach { (sourceType, itemIds) ->
            val sourceNames = sourceType2SourceNames[sourceType]
            val sourceManager = sourcesManager.getBySourceType(sourceType)
            sourceNames?.forEach { sourceName ->
                subtaskFutures.add(executor.submit {
                    try {
                        sourceManager.loadByIds(
                            bvUser = bvUser,
                            sourceName = sourceName,
                            keyList = itemIds,
                            chunkConsumer = chunkConsumer)
                    } catch (e: Exception) {
                        log.error("", e)
                    }
                })
            }
        }
        return subtaskFutures
    }
}
