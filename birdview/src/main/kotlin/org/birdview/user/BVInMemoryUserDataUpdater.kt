package org.birdview.user

import org.birdview.analysis.BVDocument
import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVUserStorage
import org.birdview.user.document.BVDocumentsLoader
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVDocumentUtils
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.*
import javax.annotation.PostConstruct
import javax.inject.Named

@Named
class BVInMemoryUserDataUpdater (
        private val userStorage: BVUserStorage,
        private val documentsLoader: BVDocumentsLoader,
        private val documentStorage: BVDocumentStorage,
        private val userLog: BVUserLog
): BVUserDataUpdater, BVUserStorage.UserChangedListener {
    companion object {
        private const val DELAY_BETWEEN_UPDATES_MINUTES: Long = 30
        private const val MAX_DAYS_BACK = 30L
    }
    private val log = LoggerFactory.getLogger(BVInMemoryUserDataUpdater::class.java)
    private data class UserUpdateInfo (val bvUser:String, val timestamp: Long = 0)
    private val executor = Executors.newScheduledThreadPool(1)

    private val userFutures = ConcurrentHashMap<String, CompletableFuture<*>>()

    private val userSemaphores = ConcurrentHashMap<String, Semaphore>()

    override fun onUserDeleted(bvUser: String) {
    }

    override fun onUserCreated(bvUser: String) {
    }

    @PostConstruct
    private fun init() {
        userStorage.addUserCreatedListener(this)
        executor.scheduleWithFixedDelay(this::refreshUsers, 0, DELAY_BETWEEN_UPDATES_MINUTES, TimeUnit.MINUTES)
    }

    private fun refreshUsers() {
        requestUserRefresh(*userStorage.listUserNames().toTypedArray())
    }

    override fun requestUserRefresh(vararg bvUsers: String) {
        log.info(">>>>>>>>> Refreshing users :${bvUsers.joinToString (",")}")

        val idleBvUsers = bvUsers.filter {
            userSemaphores.computeIfAbsent(it) { Semaphore(1) }.tryAcquire()
        }
        log.info(">>>>>>>>> Idle users :${idleBvUsers.joinToString (",")}")

        idleBvUsers.forEach {
            userFutures[it] = CompletableFuture<Void>()
        }


        executor.submit {
            var endTime = ZonedDateTime.now()
            var startTime = endTime.minusDays(2).withHour(0).withMinute(0).withSecond(0).withNano(0)
            val minStartTime = ZonedDateTime.now().minusDays(MAX_DAYS_BACK)

            try {
                while (startTime > minStartTime) {
                    for (bvUser in idleBvUsers) {
                        loadUserData(bvUser, TimeIntervalFilter(after = startTime, before = endTime))
                    }
                    endTime = startTime
                    startTime = endTime.minusDays(10)

                }
            } catch (e: Error) {
                log.error("", e)
            } finally {
                idleBvUsers.forEach {
                    userSemaphores.get(it)?.release()
                    userFutures[it]?.complete(null)
                }
                log.info(">>>>>>>>> Finished refreshing users ${bvUsers.joinToString (",")}. " +
                        "Overall ${documentStorage.count()} documents loaded.")
            }
        }
    }

    override fun waitForUserUpdated(bvUser: String) {
        userFutures[bvUser]?.get()
    }

    private fun loadUserData(bvUser: String, interval: TimeIntervalFilter) {
        log.info(">>>>>>>>> Loading data for '${bvUser}' (${BVDateTimeUtils.format(interval)})")
        val logId = userLog.logMessage(bvUser, "Updating interval ${BVDateTimeUtils.format(interval)}")
        try {

            // Loading direct docs:
            val loadedDocs = ConcurrentHashMap<String, BVDocument>()
            documentsLoader.loadDocuments(bvUser, interval) { doc ->
                documentStorage.updateDocument(doc)
                loadedDocs[doc.internalId] = doc
            }.forEach(this::waitForCompletion)

            // Loading missed referred docs:
            var loadedReferredDocs: Collection<BVDocument> = loadedDocs.values
            for (i in 1..5) {
                log.info(">>>>>>>>> Loading missed docs for '${bvUser}' (${BVDateTimeUtils.format(interval)}) #$i")

                loadedReferredDocs = loadReferredDocs(bvUser, loadedReferredDocs)

                if (loadedReferredDocs.isEmpty()) {
                    break
                }
            }
        } catch (e: Exception) {
            log.error("", e)
        } finally {
            log.info(">>>>>>>>> Loaded data for user ${bvUser} ${BVDateTimeUtils.format(interval)},${documentStorage.count()} documents.")
            userLog.logMessage(bvUser, "Updated interval ${BVDateTimeUtils.format(interval)}", logId)
        }
    }

    private fun loadReferredDocs(bvUser: String, originalDocs: Collection<BVDocument>): Collection<BVDocument> {
        val referredDocsIds = BVDocumentUtils.getReferencedDocIds(originalDocs)
        val missedDocsIds = referredDocsIds.filter { !documentStorage.containsDocWithExternalId(it) }
        val loadedReferredDocs = ConcurrentHashMap<String, BVDocument>()
        log.info("Referred docs:{}, missed docs:{}", referredDocsIds.size, missedDocsIds.size)
        documentsLoader.loadDocsByIds(bvUser, missedDocsIds) { docChunk ->
            docChunk.forEach { doc->
                documentStorage.updateDocument(doc)
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
