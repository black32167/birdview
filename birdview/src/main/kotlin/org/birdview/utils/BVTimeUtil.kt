package org.birdview.utils

import org.slf4j.LoggerFactory

object BVTimeUtil {
    private val log = LoggerFactory.getLogger(BVTimeUtil::class.java)
    fun <T>logTime (msg: String, invocation: () -> T): T {
        val start = System.currentTimeMillis()
        try {
            return invocation()
        } finally {
            val durationMs = System.currentTimeMillis() - start
            if (durationMs > 0) {
                log.warn("${msg} took ${durationMs} ms.")
            }
        }
    }
}