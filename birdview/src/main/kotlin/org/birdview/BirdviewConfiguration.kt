package org.birdview

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@Configuration
@ComponentScan
@EnableCaching
open class BirdviewConfiguration {
    @Bean
    open fun cacheManager(): CacheManager? {
        return ConcurrentMapCacheManager("bv")
    }
}