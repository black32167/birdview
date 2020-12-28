package org.birdview.source.http

import javax.ws.rs.core.GenericType

interface BVHttpClient {
    fun <T> get(
        resultClass: Class<T>,
        subPath: String? = null,
        parameters: Map<String, Any> = emptyMap()
    ): T

    fun <T> post(
        resultClass: Class<T>,
        postEntity: Any,
        subPath: String? = null,
        parameters: Map<String, Any> = emptyMap()
    ): T

    fun <T> post(
        resultType: GenericType<T>,
        postEntity: Any,
        subPath: String? = null,
        parameters: Map<String, Any> = emptyMap()): T
}
