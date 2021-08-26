package org.birdview.web

import org.springframework.web.servlet.support.ServletUriComponentsBuilder

object WebUtils {
    private val scheme:String? = System.getenv("BV_SCHEMA")
    fun getBaseUrl():String {
        val builder = ServletUriComponentsBuilder.fromCurrentContextPath()
        if (scheme != null) {
            builder.scheme(scheme)
        }
        return builder.build().toUriString()
    }
}