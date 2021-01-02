package org.birdview.source.http.log.record

import org.birdview.config.BVFoldersConfig
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.utils.JsonDeserializer
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import org.springframework.core.annotation.Order
import javax.inject.Named

@Named
@Profile("record-responses")
class BVLoggingHttpClientFactoryBeanPostProcessor(
    private val foldersConfig: BVFoldersConfig,
    private val jsonDeserializer: JsonDeserializer
): BeanPostProcessor, Ordered {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any =
        (bean as? BVHttpClientFactory)
            ?.let { BVLoggingHttpClientFactory(it, jsonDeserializer, foldersConfig) }
            ?: bean

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}