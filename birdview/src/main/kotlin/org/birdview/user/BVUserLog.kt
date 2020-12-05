package org.birdview.user

import org.birdview.utils.BVDateTimeUtils
import org.birdview.web.explore.model.BVUserLogEntry
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class BVUserLog {
    private val maxUserLogSize = 7

    private val user2Log = ConcurrentHashMap<String, MutableList<BVUserLogEntry>>()

    fun logMessage(bvUser: String, message: String) {
        val userLog = getOrCreateUserLog(bvUser)
        if (userLog.size > maxUserLogSize) {
            userLog.removeLast()
        }
        userLog += BVUserLogEntry(
                timestamp = BVDateTimeUtils.timeFormat(ZonedDateTime.now()),
                message = message
        )
    }

    fun getUserLog(bvUser: String): List<BVUserLogEntry> =
            user2Log[bvUser]?.reversed()
                    ?: emptyList()

    private fun getOrCreateUserLog(bvUser: String) =
            user2Log.computeIfAbsent(bvUser) { ArrayList() }
}