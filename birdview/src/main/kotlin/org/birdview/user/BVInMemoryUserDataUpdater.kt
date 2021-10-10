package org.birdview.user

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.birdview.analysis.BVDocument
import org.birdview.config.EnvironmentVariables
import org.birdview.model.RelativeHierarchyType.LINK_TO_PARENT
import org.birdview.model.RelativeHierarchyType.UNSPECIFIED
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVUserStorage
import org.birdview.time.BVTimeService
import org.birdview.user.document.BVDocumentsLoader
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVDocumentUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.*
import javax.inject.Named

@Named
class BVInMemoryUserDataUpdater (
        private val userStorage: BVUserStorage,
        private val documentsLoader: BVDocumentsLoader,
        private val documentStorage: BVDocumentStorage,
        private val userLog: BVUserLog,
        private val timeService: BVTimeService
): BVUserDataUpdater, SmartInitializingSingleton {
    companion object {
        private const val DELAY_BETWEEN_UPDATES_SECONDS: Long = 30 * 60
        private const val MAX_DAYS_BACK = 30L
    }
    private val log = LoggerFactory.getLogger(BVInMemoryUserDataUpdater::class.java)
    private data class UserUpdateInfo (val bvUser:String, val timestamp: Long = 0)
    private val updateScheduleExecutor = Executors.newScheduledThreadPool(1, ThreadFactoryBuilder().setNameFormat("UpdateScheduler-%d").build())
    private val userUpdateExecutor = Executors.newFixedThreadPool(3, ThreadFactoryBuilder().setNameFormat("UserUpdater-%d").build())

    private val userFutures = ConcurrentHashMap<String, Future<*>>()

    private val userSemaphores = ConcurrentHashMap<String, Semaphore>()

    override fun afterSingletonsInstantiated() {
        updateScheduleExecutor.scheduleWithFixedDelay(this::refreshUsers, 1, DELAY_BETWEEN_UPDATES_SECONDS, TimeUnit.SECONDS)
    }

    private fun refreshUsers() {
        try {
            log.info("Pulling initial information from users sources")
            requestUserRefresh(*userStorage.listUserNames().toTypedArray())
        } catch (e: Exception) {
            log.error("Failed pull initial information from users sources", e)
        }
    }

    override fun requestUserRefresh(vararg bvUsers: String) {
        log.info(">>>>>>>>> Refreshing users :${bvUsers.joinToString (",")}")

        val idleBvUsers = bvUsers.filter {
            userSemaphores.computeIfAbsent(it) { Semaphore(1) }.tryAcquire()
        }
        log.info(">>>>>>>>> Idle users :${idleBvUsers.joinToString (",")}")

        updateScheduleExecutor.submit {
            for (bvUser in idleBvUsers) {
                val now = System.getenv(EnvironmentVariables.END_UPDATE_PERIOD)?.let { BVDateTimeUtils.parse(it, "dd-MM-yyyy") }
                    ?: timeService.getTodayInUserZone(bvUser)
                userFutures[bvUser] = userUpdateExecutor.submit {
                    var endTime: OffsetDateTime? = null
                    var startTime = now.minusDays(2).withHour(0).withMinute(0).withSecond(0).withNano(0)
                    val minStartTime = now.minusDays(MAX_DAYS_BACK)
                    while (startTime > minStartTime) {
                        loadUserData(bvUser, TimeIntervalFilter(after = startTime, before = endTime))
                        endTime = startTime
                        startTime = startTime.minusDays(10)
                    }
                }
            }

            userFutures.forEach { bvUser, future->
                try {
                    future.get()
                } catch (e: Throwable) {
                    log.error("", e)
                } finally {
                    userSemaphores.get(bvUser)?.release()
                }
            }

            log.info(">>>>>>>>> Finished refreshing users ${bvUsers.joinToString (",")}. " +
                    "Overall ${documentStorage.count()} documents loaded.")
        }
    }

    override fun waitForUserUpdated(bvUser: String) {
        userFutures[bvUser]?.get()
    }

    private fun loadUserData(bvUser: String, interval: TimeIntervalFilter) {
        log.info(">>>>>>>>> Loading data for '${bvUser}' (${BVDateTimeUtils.offsetFormat(interval)})")
        val logId = userLog.logMessage(bvUser, "Updating interval ${BVDateTimeUtils.localDateFormat(interval)}")

        val loadedDocs = ConcurrentHashMap<String, BVDocument>()
        val loadedIds = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
        val documentSessionConsumer = object:BVSessionDocumentConsumer {
            override fun consume(documents: List<BVDocument>) {
                documents.forEach { doc->
                    documentStorage.updateDocument(doc, bvUser)
                    doc.ids.forEach { loadedIds += it.id }
                    loadedDocs[doc.internalId] = doc
                }
            }
            override fun isConsumed(externalId: String): Boolean = loadedIds.contains(externalId)
        }
        try {
            // Loading direct docs:
            documentsLoader.loadDocuments(bvUser, interval, documentSessionConsumer)
                .forEach(this::waitForCompletion)

            // Loading missed referred docs:
            var loadedReferredDocs: Collection<BVDocument> = loadedDocs.values
            for (i in 1..2) {
                log.info(">>>>>>>>> Loading missed docs for '${bvUser}' (${BVDateTimeUtils.offsetFormat(interval)}) #$i")

                loadedReferredDocs = loadReferredDocs(bvUser, loadedReferredDocs)

                if (loadedReferredDocs.isEmpty()) {
                    break
                }
            }
        } catch (e: Exception) {
            log.error("", e)
        } finally {
            log.info(">>>>>>>>> Loaded data for user ${bvUser} ${BVDateTimeUtils.offsetFormat(interval)},${documentStorage.count()} documents.")
            userLog.logMessage(bvUser, "Updated interval ${BVDateTimeUtils.localDateFormat(interval)}", logId)
        }
    }

    private fun loadReferredDocs(bvUser: String, originalDocs: Collection<BVDocument>): Collection<BVDocument> {
        val referredDocsIds = BVDocumentUtils.getReferencedDocIdsByHierarchyType(
            originalDocs, setOf(LINK_TO_PARENT, UNSPECIFIED))
        val missedDocsIds = referredDocsIds.filter {
            !documentStorage.containsDocWithExternalId(it)
        }
        val loadedReferredDocs = ConcurrentHashMap<String, BVDocument>()
        log.info("Referred docs:{}, missed docs:{}", referredDocsIds.size, missedDocsIds.size)
        documentsLoader.loadDocsByIds(bvUser, missedDocsIds) { docChunk ->
            docChunk.forEach { doc->
                documentStorage.updateDocument(doc, bvUser)
                loadedReferredDocs[doc.internalId] = doc
            }
        }.forEach(this::waitForCompletion)
        return loadedReferredDocs.values
    }

    private fun waitForCompletion(future: Future<*>) {
        try {
            future.get()
        } catch (e: Exception) {
            log.error(e.message)
        }
    }
}
