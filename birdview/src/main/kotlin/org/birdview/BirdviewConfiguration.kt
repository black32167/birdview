package org.birdview

import org.birdview.BVCacheNames.SOURCE_SECRET_CACHE_NAME
import org.birdview.BVCacheNames.USER_NAMES_CACHE
import org.birdview.BVCacheNames.USER_SETTINGS_CACHE
import org.birdview.BVCacheNames.USER_SOURCE_CACHE
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean

@EnableCaching
@SpringBootApplication
open class BirdviewConfiguration {
    @Bean
    open fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager(
                USER_SETTINGS_CACHE, USER_NAMES_CACHE, USER_SOURCE_CACHE, SOURCE_SECRET_CACHE_NAME)
    }
}