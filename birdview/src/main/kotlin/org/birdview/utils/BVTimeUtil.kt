package org.birdview.utils

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

object BVTimeUtil {
    private class Stat(val tag: String) {
        private val totalTimeMs = AtomicLong()
        private val start = System.currentTimeMillis()
        private val end = AtomicLong(start)
        private val hits = AtomicInteger()
        fun finished(executionTime:Long) {
            val invocationEndTime = System.currentTimeMillis()
            totalTimeMs.addAndGet(executionTime)
            hits.incrementAndGet()
            end.updateAndGet { it.coerceAtLeast(invocationEndTime) }
        }
        override fun toString() = "$tag took ${totalTimeMs.get()} ms. in total, was invoked ${hits.get()} times, concurrent duration was ${end.get() - start} ms."
    }
    private val log = LoggerFactory.getLogger(BVTimeUtil::class.java)
    private val stats = ConcurrentHashMap<String, Stat>()

    fun <T>logTimeAndReturn (tag: String, invocation: () -> T): T {
        return logTimeAndMaybeReturn(tag, invocation)!!
    }

    fun <T>logTimeAndMaybeReturn (tag: String, invocation: () -> T): T? {
        val start = System.currentTimeMillis()
        val stat = stats.computeIfAbsent(tag) { Stat(tag) }
        try {
            return invocation()
        } finally {
            val durationMs = System.currentTimeMillis() - start
            stat.finished(durationMs)
            if (durationMs > 0) {
                log.warn("${tag} took ${durationMs} ms.")
            }
        }
    }

    fun printStats() {
        val buffer = StringBuilder("Time stats:").appendln()
        stats.values.forEach {
            buffer.appendln("  $it")
        }
        stats.clear()
        log.info(buffer.toString())
    }
}