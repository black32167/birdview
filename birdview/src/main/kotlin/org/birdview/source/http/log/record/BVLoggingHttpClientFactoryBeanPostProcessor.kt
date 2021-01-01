package org.birdview.source.http.log.record

import org.birdview.config.BVFoldersConfig
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.utils.JsonDeserializer
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Profile
import javax.inject.Named

@Named
@Profile("record-responses")
class BVLoggingHttpClientFactoryBeanPostProcessor(
    private val foldersConfig: BVFoldersConfig,
    private val jsonDeserializer: JsonDeserializer
): BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any =
        (bean as? BVHttpClientFactory)
            ?.let { BVLoggingHttpClientFactory(it, jsonDeserializer, foldersConfig) }
            ?: bean
}