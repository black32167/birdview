package org.birdview.utils

import java.util.concurrent.ThreadFactory

object BVConcurrentUtils {
    fun getDaemonThreadFactory() = ThreadFactory { runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread
    }
}