package org.birdview

import org.birdview.BVCacheNames.DEFAULT_SOURCE_CACHE_NAME
import org.birdview.BVCacheNames.HTTP_CLIENT_CACHE_NAME
import org.birdview.BVCacheNames.SOURCE_OAUTH_TOKENS_CACHE_NAME
import org.birdview.BVCacheNames.USER_NAMES_CACHE
import org.birdview.BVCacheNames.USER_SETTINGS_CACHE
import org.birdview.BVCacheNames.USER_SOURCE_CACHE
import org.birdview.BVCacheNames.USER_SOURCE_SECRET_CACHE_NAME
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean

@EnableCaching(order = 10)
@SpringBootApplication
open class BirdviewConfiguration {
    @Bean
    open fun cacheManager(): CacheManager = ConcurrentMapCacheManager(
                 USER_SETTINGS_CACHE,
                 USER_NAMES_CACHE,
                 USER_SOURCE_CACHE,
                 DEFAULT_SOURCE_CACHE_NAME,
                 USER_SOURCE_SECRET_CACHE_NAME,
                 HTTP_CLIENT_CACHE_NAME,
                 SOURCE_OAUTH_TOKENS_CACHE_NAME)
}
