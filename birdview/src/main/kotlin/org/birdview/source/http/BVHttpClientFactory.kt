package org.birdview.source.http

import org.birdview.BVCacheNames
import org.birdview.utils.remote.ApiAuth
import org.springframework.cache.annotation.Cacheable

interface BVHttpClientFactory {
    @Cacheable(cacheNames = arrayOf(BVCacheNames.HTTP_CLIENT_CACHE_NAME), key = "#url")
    fun getHttpClient(url:String, authProvider:() -> ApiAuth? = {null}): BVHttpClient
}