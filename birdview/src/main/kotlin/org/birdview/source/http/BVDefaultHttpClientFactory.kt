package org.birdview.source.http

import org.birdview.utils.remote.ApiAuth
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class BVDefaultHttpClientFactory: BVHttpClientFactory {
    private val log = LoggerFactory.getLogger(BVDefaultHttpClientFactory::class.java)
    init {
        log.info("BVDefaultHttpClientFactory created")
    }
    override fun getHttpClientAuthenticated(url: String, authProvider: () -> ApiAuth?): BVHttpClient {
        log.info("Creating authenticated HTTP client for {}", url)
        return BVHttpClientImpl(basePath = url, authProvider = authProvider)
    }

    override fun getHttpClientUnauthenticated(url: String): BVHttpClient {
        log.info("Creating unauthenticated HTTP client for {}", url)
        return BVHttpClientImpl(basePath = url, authProvider = {null})
    }
}
