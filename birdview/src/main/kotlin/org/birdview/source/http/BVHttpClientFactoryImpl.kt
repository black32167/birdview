package org.birdview.source.http

import org.birdview.utils.remote.ApiAuth
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class BVHttpClientFactoryImpl: BVHttpClientFactory {
    private val url2Clients:MutableMap<String, BVHttpClient> = ConcurrentHashMap()

    override fun getHttpClient(url: String, authProvider: () -> ApiAuth): BVHttpClient =
        url2Clients.computeIfAbsent(url) {
            BVHttpClientImpl(basePath = url, authProvider = authProvider)
        }
}