package org.birdview.user

import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVUserStorage
import org.birdview.user.document.BVDocumentsLoader
import org.birdview.utils.BVDateTimeUtils
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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

    private val userUpdateTimestampInfoHeap = PriorityQueue<UserUpdateInfo> { userInfo1, userInfo2->
        (userInfo1.timestamp-userInfo2.timestamp).toInt()
    }

    private val userFutures = ConcurrentHashMap<String, CompletableFuture<*>>()

    override fun onUserDeleted(bvUser: String) {
        userUpdateTimestampInfoHeap
                .find { it.bvUser == bvUser }
                ?.also {
                    userUpdateTimestampInfoHeap.remove(it)
                }
    }

    override fun onUserCreated(bvUser: String) {
        userUpdateTimestampInfoHeap.add(UserUpdateInfo(bvUser))
    }

    @PostConstruct
    private fun init() {
        userStorage.listUserNames().forEach { userName->
            userUpdateTimestampInfoHeap.add(UserUpdateInfo(userName))
        }
        userStorage.addUserCreatedListener(this)
        executor.scheduleWithFixedDelay(this::refreshMostStaleUser, 0, DELAY_BETWEEN_UPDATES_MINUTES, TimeUnit.MINUTES)
    }

    private fun refreshMostStaleUser() {
        val maybeUserInfo: UserUpdateInfo?
        synchronized(userUpdateTimestampInfoHeap) {
            maybeUserInfo = userUpdateTimestampInfoHeap.poll()
        }

        if (maybeUserInfo != null) {
            requestUserRefresh(maybeUserInfo.bvUser)
        }
    }

    override fun requestUserRefresh(bvUser: String) {
        log.info(">>>>>>>>> Refreshing user ${bvUser}")

        executor.submit {
            var endTime = ZonedDateTime.now()
            var startTime = endTime.minusDays(10)
            val minStartTime = ZonedDateTime.now().minusDays(MAX_DAYS_BACK)

            while (startTime > minStartTime) {
                loadUserData(bvUser, TimeIntervalFilter(after = startTime, before = endTime))
                endTime = startTime
                startTime = endTime.minusDays(14)
            }
        }
    }

    override fun waitForUserUpdated(bvUser: String) {
        userFutures[bvUser]?.get()
    }

    private fun loadUserData(bvUser: String, interval: TimeIntervalFilter) {
        val userFuture = CompletableFuture<Void>()
        try {
            userFutures[bvUser] = userFuture
            userUpdateTimestampInfoHeap.offer(UserUpdateInfo(bvUser = bvUser, timestamp = System.currentTimeMillis()))

            userLog.logMessage(bvUser, "Start updating interval ${BVDateTimeUtils.format(interval)}")

            val futures = documentsLoader.loadDocuments(bvUser, interval) { doc ->
                documentStorage.updateDocument(doc)
            }

            for (future in futures) {
                try {
                    future.get()
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
            log.error("", e)
        } finally {
            userLog.logMessage(bvUser, "Updated interval ${BVDateTimeUtils.format(interval)}")
            userFuture.complete(null)
            log.info(">>>>>>>>> Finished refreshing user ${bvUser}")
        }
    }
}
