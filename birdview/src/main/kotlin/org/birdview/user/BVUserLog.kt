package org.birdview.user

import org.birdview.storage.BVUserStorage
import org.birdview.time.BVTimeService
import org.birdview.utils.BVDateTimeUtils
import org.birdview.web.explore.model.BVUserLogEntry
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class BVUserLog(
    private val userStorage: BVUserStorage,
    private val timeService: BVTimeService
) {
    private val maxUserLogSize = 7

    // userId->logId->LogEntry
    private val user2Log = ConcurrentHashMap<String, MutableList<BVUserLogEntry>>()

    fun logMessage(bvUser: String, message: String, idReference: String? = null): String {
        val userLog = getOrCreateUserLog(bvUser)
        val userProfile = userStorage.getUserSettings(bvUser)
        val userTimeZone = ZoneId.of(userProfile.zoneId)
        synchronized(userLog) {
            val existingEntry = if (idReference != null) {
                userLog.firstOrNull { it.id == idReference }
            } else null

            if (existingEntry == null) {
                if (userLog.size > maxUserLogSize) {
                    userLog.removeFirst()
                }
                val newEntry = BVUserLogEntry(
                        timestamp = BVDateTimeUtils.timeFormat(timeService.getNow(userTimeZone)),
                        message = message)
                userLog += newEntry
                return newEntry.id
            } else {
                existingEntry.message = message
                return existingEntry.id
            }
        }
    }

    fun getUserLog(bvUser: String): List<BVUserLogEntry> {
        val userLog = user2Log[bvUser]
        if (userLog != null) {
            synchronized(userLog) {
                return userLog.reversed()
            }
        }
        return emptyList()
    }

    private fun getOrCreateUserLog(bvUser: String) =
            user2Log.computeIfAbsent(bvUser) { ArrayList() }
}