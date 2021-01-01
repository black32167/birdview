package org.birdview.source.response.log

import org.birdview.source.BVTaskSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Profile
import javax.inject.Named

@Named
//@Profile("record-responses")
class SourceLoggingBeanPostProcessor: BeanPostProcessor {
    private val log = LoggerFactory.getLogger(SourceLoggingBeanPostProcessor::class.java)

    init {
        log.info("Initializing {}", javaClass.simpleName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any =
        (bean as? BVTaskSource)?.let { LoggingDelegatingTaskSource(it) } ?: bean
}