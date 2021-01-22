package org.birdview.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.ThreadFactory

object BVConcurrentUtils {
    fun getDaemonThreadFactory() = ThreadFactory { runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread
    }

    fun getDaemonThreadFactory(name: String) = ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat(name)
        .build()
}