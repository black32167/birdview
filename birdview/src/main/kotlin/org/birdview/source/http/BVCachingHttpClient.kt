package org.birdview.source.http

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class BVCachingHttpClient(val delegate: BVHttpClient): BVHttpClient {
    private val log = LoggerFactory.getLogger(BVCachingHttpClient::class.java)
    init {
        log.info("BVCachingHttpClient created")
    }
    private data class CacheKey (
        val prefix: String,
        val resultClass: Class<*>,
        val path: String?,
        val parameters: Map<String, Any> = emptyMap())

    private val cache: Cache<CacheKey, Any> = CacheBuilder
        .newBuilder()
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .build()

    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T {
        val key = CacheKey("GET", resultClass, subPath, parameters)
        return cache.get(key) {
            delegate.get(resultClass, subPath, parameters)
        } as T
    }

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T {
        return delegate.post(resultClass, postEntity, subPath, parameters)
    }

    override fun <T> postForm(resultClass: Class<T>, subPath: String?, formFields: Map<String, String>): T {
        return delegate.postForm(resultClass, subPath, formFields)
    }
}