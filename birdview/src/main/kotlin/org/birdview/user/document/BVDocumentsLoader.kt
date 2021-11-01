package org.birdview.user.document

import org.birdview.analysis.BVDocument
import org.birdview.config.EnvironmentVariables
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.SourceType
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVSourcesProvider
import org.birdview.time.BVTimeService
import org.birdview.user.BVUserLog
import org.birdview.utils.BVConcurrentUtils
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVTimeUtil
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Named

@Named
class BVDocumentsLoader (
    private val sourcesManager: BVSourcesProvider,
    private val sourceConfigProvider: BVSourceConfigProvider,
    private val documentStorage: BVDocumentStorage,
    private val timeService: BVTimeService,
    private val userLog: BVUserLog,
) {
    companion object {
        private const val MAX_DAYS_BACK = 30L
    }

    private val log = LoggerFactory.getLogger(BVDocumentsLoader::class.java)
    private val executor = Executors.newCachedThreadPool(BVConcurrentUtils.getDaemonThreadFactory("DocLoader-%d"))

    fun loadDocuments(
        bvUser: String,
        documentConsumer: BVSessionDocumentConsumer
    ): List<Future<*>> =
        sourceConfigProvider.listEnabledSourceConfigs(bvUser)
            .map { sourceConfig ->
                val sourceManager = sourcesManager.getBySourceType(sourceConfig.sourceType)
                val lastUpdatedTime = documentStorage.getLastUpdatedDocument(bvUser = bvUser, sourceName = sourceConfig.sourceName)

                val now = System.getenv(EnvironmentVariables.END_UPDATE_PERIOD)?.let { BVDateTimeUtils.parse(it, "dd-MM-yyyy") }
                    ?: timeService.getTodayInUserZone(bvUser)
                val minStartTime = lastUpdatedTime ?: now.minusDays(MAX_DAYS_BACK)
                var timeInterval = TimeIntervalFilter(
                    after = maxOf(
                        minStartTime,
                        now.minusDays(2).withHour(0).withMinute(0).withSecond(0).withNano(0)),
                    before = null)

                CompletableFuture.runAsync(Runnable {
                    while (timeInterval.after >= minStartTime) {
                        log.info("Loading data from ${sourceConfig.sourceName} for ${bvUser}...")
                        val logId = userLog.logMessage(bvUser, "Updating ${sourceConfig.sourceName} (${BVDateTimeUtils.localDateFormat(timeInterval)})")
                        BVTimeUtil.logTimeAndReturn("Loading data from ${sourceConfig.sourceType} for ${bvUser} (${BVDateTimeUtils.offsetFormat(timeInterval)})") {
                            try {
                                sourceManager.getTasks(
                                    bvUser,
                                    timeInterval,
                                    sourceConfig,
                                    documentConsumer
                                )
                            } catch (e: Throwable) {
                                log.error(
                                    "Error loading documents for ${sourceConfig.sourceType}, sm=${sourceManager.getType()} user=${bvUser}:",
                                    e
                                )
                            }
                            timeInterval = timeInterval.copy(
                                after = timeInterval.after.minusDays(10),
                                before = timeInterval.after
                            )
                        }
                        userLog.logMessage(bvUser, "Updated ${sourceConfig.sourceName} (${BVDateTimeUtils.localDateFormat(timeInterval)})", logId)
                    }
                    log.info("Loaded data from ${sourceConfig.sourceType} for ${bvUser} (${BVDateTimeUtils.offsetFormat(timeInterval)})")
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
