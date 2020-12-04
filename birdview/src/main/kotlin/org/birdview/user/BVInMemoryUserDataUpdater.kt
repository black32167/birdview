package org.birdview.user

import org.birdview.model.TimeIntervalFilter
import org.birdview.storage.BVDocumentStorage
import org.birdview.storage.BVUserStorage
import org.birdview.user.document.BVDocumentsLoader
import org.birdview.web.explore.model.BVUserDocumentCorpusStatus
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
        private val documentStorage: BVDocumentStorage
): BVUserDataUpdater, BVUserStorage.UserChangedListener {
    companion object {
        private const val DELAY_BETWEEN_UPDATES_MINUTES: Long = 30
    }
    private val log = LoggerFactory.getLogger(BVInMemoryUserDataUpdater::class.java)
    private data class UserUpdateInfo (val bvUser:String, val timestamp: Long = 0)
    private val executor = Executors.newScheduledThreadPool(1)

    private val userUpdateTimestampInfoHeap = PriorityQueue<UserUpdateInfo> { userInfo1, userInfo2->
        (userInfo1.timestamp-userInfo2.timestamp).toInt()
    }

    @Deprecated("in favor of userUpdateStatus")
    private val userFutures = ConcurrentHashMap<String, CompletableFuture<*>>()

    private val userUpdateStatus = ConcurrentHashMap<String, BVUserDocumentCorpusStatus>()

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
            refreshUser(maybeUserInfo.bvUser)
        }
    }

    override fun refreshUser(bvUser: String) {
        log.info(">>>>>>>>> Refreshing user ${bvUser}")
        executor.submit {
            val userFuture = CompletableFuture<Void>()
            userFutures[bvUser] = userFuture
            userUpdateTimestampInfoHeap.offer(UserUpdateInfo(bvUser = bvUser, timestamp = System.currentTimeMillis()))


            val interval = TimeIntervalFilter(after = ZonedDateTime.now().minusMonths(1))

            val futures = documentsLoader.loadDocuments(bvUser, interval) { doc ->
                documentStorage.updateDocument(doc)
                userUpdateStatus[bvUser]
            }

            documentCorpusStatus(bvUser).startUpdating()
            try {
                for (future in futures) {
                    try {
                        future.get()
                    } catch (e: Exception) {
                    }
                }
            } finally {
                documentCorpusStatus(bvUser).finishUpdating()
                userFuture.complete(null)
                log.info(">>>>>>>>> Finished refreshing user ${bvUser}")
            }
        }
    }

    private fun documentCorpusStatus(bvUser: String) =
            userUpdateStatus
                .computeIfAbsent(bvUser) { BVUserDocumentCorpusStatus() }

    override fun waitForUserUpdated(userAlias: String) {
        userFutures[userAlias]?.get()
    }

    override fun getStatusForUser(bvUser: String): BVUserDocumentCorpusStatus? =
            userUpdateStatus[bvUser]
}
