package org.birdview.profile

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.birdview.utils.BVTimeUtil
import org.springframework.context.annotation.Configuration

@Aspect
@Configuration
open class TimeLoggingAspect {
    @Around("within(@org.springframework.stereotype.Repository *) && execution(* *(..))")
    fun aroundRepositoryMethods(pjp: ProceedingJoinPoint): Any {
        val methodName = pjp.signature.name
        return BVTimeUtil.logTime(methodName, pjp::proceed)
    }
}