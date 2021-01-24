package org.birdview.source.http

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Configuration
open class BVCachingHttpClientFactoryAspect {
    private val log = LoggerFactory.getLogger(BVCachingHttpClientFactoryAspect::class.java)
    init {
        log.info("BVCachingHttpClientFactoryAspect created")
    }

    @Around("execution(* org.birdview.source.http.BVHttpClientFactory.*(..))")
    fun aroundHttpGet(pjp: ProceedingJoinPoint): Any {
        val httpClient:BVHttpClient = pjp.proceed() as BVHttpClient
        return BVCachingHttpClient(httpClient)//httpClientFactory.getHttpClient(url = url, authProvider = authProvider))
    }
}
