package org.birdview.source.http

import org.birdview.BVCacheNames
import org.birdview.utils.remote.ApiAuth
import org.springframework.cache.annotation.Cacheable

interface BVHttpClientFactory {
    fun getHttpClientAuthenticated(url:String, authProvider:() -> ApiAuth?): BVHttpClient

    @Cacheable(cacheNames = arrayOf(BVCacheNames.HTTP_CLIENT_CACHE_NAME), key = "#url.toString()", sync = true)
    fun getHttpClientUnauthenticated(url:String): BVHttpClient
}