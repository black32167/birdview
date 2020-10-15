package org.birdview

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
        return ConcurrentMapCacheManager("bv", "sourcesConfig", "userProfilesConfig")
    }
}