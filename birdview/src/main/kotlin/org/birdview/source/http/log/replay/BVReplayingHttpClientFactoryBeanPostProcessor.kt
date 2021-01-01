package org.birdview.source.http.log.replay

import org.birdview.config.BVFoldersConfig
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.utils.JsonDeserializer
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Profile
import javax.inject.Named

@Named
@Profile("replay-responses")
class BVReplayingHttpClientFactoryBeanPostProcessor(
    private val foldersConfig: BVFoldersConfig,
    private val jsonDeserializer: JsonDeserializer
): BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any =
        (bean as? BVHttpClientFactory)
            ?.let { BVReplayingHttpClientFactory(foldersConfig, jsonDeserializer) }
            ?: bean
}