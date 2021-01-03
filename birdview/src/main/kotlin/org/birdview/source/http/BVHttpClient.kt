package org.birdview.source.http

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

    fun <T> postForm(
        resultClass: Class<T>, subPath: String? = null, formFields: Map<String, String> = emptyMap()): T
}
