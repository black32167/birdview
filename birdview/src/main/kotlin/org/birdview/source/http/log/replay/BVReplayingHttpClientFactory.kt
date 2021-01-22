package org.birdview.source.http.log.replay

import org.birdview.config.BVFoldersConfig
import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.utils.JsonDeserializer
import org.birdview.utils.remote.ApiAuth
import org.slf4j.LoggerFactory

class BVReplayingHttpClientFactory(
    private val foldersConfig:BVFoldersConfig,
    private val jsonDeserializer: JsonDeserializer
): BVHttpClientFactory {
    private val log = LoggerFactory.getLogger(BVReplayingHttpClientFactory::class.java)
    init {
        log.info("BVReplayingHttpClientFactory created")
    }
    override fun getHttpClient(url: String, authProvider: () -> ApiAuth?): BVHttpClient =
        BVReplayingHttpClient(foldersConfig.getHttpInteractionsLogFolder(), jsonDeserializer)
}
