package org.birdview.source.http.log.replay

import org.birdview.config.BVFoldersConfig
import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.utils.JsonDeserializer
import org.birdview.utils.remote.ApiAuth

class BVReplayingHttpClientFactory(
    private val foldersConfig:BVFoldersConfig,
    private val jsonDeserializer: JsonDeserializer
): BVHttpClientFactory {
    override fun getHttpClient(url: String, authProvider: () -> ApiAuth?): BVHttpClient =
        BVReplayingHttpClient(foldersConfig.getHttpInteractionsLogFolder(), jsonDeserializer)
}
