package org.birdview.web.binding

import org.birdview.web.form.CreateUserSourceFormData
import org.birdview.web.form.UpdateUserSourceFormData
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import javax.servlet.http.HttpServletRequest

class SourceFormArgumentsResolver: HandlerMethodArgumentResolver {
    private val supportedClasses = arrayOf(
        CreateUserSourceFormData::class.java,
        UpdateUserSourceFormData::class.java)

    override fun supportsParameter(methodParameter: MethodParameter): Boolean =
        supportedClasses.contains(methodParameter.parameterType)

    override fun resolveArgument(
        methodParameter: MethodParameter,
        modelAndViewContainer: ModelAndViewContainer?,
        nativeWebRequest: NativeWebRequest,
        webDataBinderFactory: WebDataBinderFactory?
    ): Any {
        val request = nativeWebRequest.nativeRequest as HttpServletRequest
        val data = FormBindingUtil.create(request, methodParameter.parameterType.kotlin)
        return data
    }
}