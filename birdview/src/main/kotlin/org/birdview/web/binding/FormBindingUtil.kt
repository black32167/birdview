package org.birdview.web.binding

import org.birdview.utils.ParameterResolver
import org.birdview.utils.ReflectiveObjectMapper
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.BadRequestException
import kotlin.reflect.KClass

object FormBindingUtil {
    fun <T: Any> create (request: HttpServletRequest, targetClass: KClass<T>): T {
        val underlyingResolver = object: ParameterResolver {
            override fun <T : Any> resolve(name: String, classifier: KClass<T>): T? =
                if (classifier == String::class) {
                    request.getParameter(name) as T?
                } else {
                    null
                }
        }
        return ReflectiveObjectMapper.toObjectCatching(targetClass, underlyingResolver)
            ?: throw BadRequestException("Could not materialize ${targetClass.simpleName}")
    }
}