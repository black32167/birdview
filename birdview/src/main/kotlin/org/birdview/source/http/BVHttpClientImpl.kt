package org.birdview.source.http

import org.birdview.utils.remote.ApiAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.Form
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

class BVHttpClientImpl (
    basePath: String,
    authProvider: () -> ApiAuth?
): BVHttpClient {
    private val targetFactory = WebTargetFactory(
        url = basePath,
        authProvider = authProvider
    )

    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        get(subPath, parameters)
            .readEntity(resultClass)

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        post(postEntity, subPath, parameters)
            .readEntity(resultClass)

    override fun <T> post(resultType: GenericType<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        post(postEntity, subPath, parameters)
            .readEntity(resultType)

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
