package org.birdview.user

import org.birdview.storage.BVUserStorage
import org.birdview.user.document.BVDocumentsLoader
import org.slf4j.LoggerFactory
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
        private val documentsLoader: BVDocumentsLoader
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
            refreshUser(maybeUserInfo.bvUser)
        }
    }

    override fun refreshUser(bvUser: String) {
        log.info(">>>>>>>>> Refreshing user ${bvUser}")
        val userFuture = CompletableFuture<Void>()
        userFutures[bvUser] = userFuture
        userUpdateTimestampInfoHeap.offer(UserUpdateInfo(bvUser = bvUser, timestamp = System.currentTimeMillis()))
        val futures = documentsLoader.loadDocuments(bvUser)
        for (future in futures) {
            try {
                future.get()
            } catch (e: Exception) {
            }
        }
        userFuture.complete(null)
        log.info(">>>>>>>>> Finished refreshing user ${bvUser}")
    }

    override fun waitForUserUpdated(userAlias: String) {
        userFutures[userAlias]?.get()
    }
}
