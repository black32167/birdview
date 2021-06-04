package org.birdview.source.http

import org.birdview.utils.remote.ApiAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.Form
import javax.ws.rs.core.Response

class BVHttpClientImpl (
    override val basePath: String,
    authProvider: () -> ApiAuth?
): BVHttpClient {
    companion object {
        private val encounteredUrls:MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())
    }
    private val log = LoggerFactory.getLogger(BVHttpClientImpl::class.java)
    private val targetFactory = WebTargetFactory(
        url = basePath,
        authProvider = authProvider,
        enableLogging = System.getenv("BV_HTTP_TRACING_ENABLED")?.toBoolean() ?: false
    )

    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T {
//        val key = "$basePath/$subPath($parameters)"
//        if (encounteredUrls.contains(key)) {
//            log.warn(
//                "Http client was already invoked for {}, stack=\n\t{}",
//                key,
//                Thread.currentThread().stackTrace.joinToString("\n\t")
//            )
//        } else {
//            encounteredUrls += key
//        }
        return get(subPath, parameters)
            .readEntity(resultClass)
    }

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        post(postEntity, subPath, parameters)
            .readEntity(resultClass)

    override fun <T> postForm(resultClass: Class<T>, subPath: String?, formFields: Map<String, String>): T =
        request(subPath, emptyMap())
            .post(Entity.form(Form().also { form->formFields.forEach (form::param) }))
            .also(ResponseValidationUtils::validate)
            .readEntity(resultClass)

    private fun post(postEntity: Any, subPath: String?, parameters: Map<String, Any>): Response =
        request(subPath, parameters)
            .post(Entity.json(postEntity))
            .also(ResponseValidationUtils::validate)

    private fun get(path: String?, parameters: Map<String, Any>): Response =
        request(path, parameters)
            .get()
            .also(ResponseValidationUtils::validate)

    private fun request(path: String?, parameters: Map<String, Any>): Invocation.Builder {
        var target = targetFactory.getTarget(path)

        parameters.forEach { (paramName, paramValue) ->
            target = target.queryParam(paramName, paramValue)
        }

        return target.request()
    }
}
