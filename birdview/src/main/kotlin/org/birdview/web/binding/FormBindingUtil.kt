package org.birdview.web.binding

import org.birdview.utils.ReflectiveObjectMapper
import org.birdview.utils.ReflectiveParameterResolver
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.BadRequestException
import kotlin.reflect.KClass

object FormBindingUtil {
    fun <T: Any> create (request: HttpServletRequest, targetClass: KClass<T>): T {
        return ReflectiveObjectMapper.toObjectCatching(targetClass, ReflectiveParameterResolver { name ->
            request.getParameter(name)
        })
            ?: throw BadRequestException("Could not materialize ${targetClass.simpleName}")
    }
}