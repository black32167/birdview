package org.birdview.utils

import java.util.concurrent.ThreadFactory

object BVConcurrentUtils {
    fun getDaemonThreadFactory() = ThreadFactory { runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread
    }

    fun getDaemonThreadFactory(name: String) = ThreadFactory { runnable ->
        val thread = Thread(runnable, name)
        thread.isDaemon = true
        thread
    }
}