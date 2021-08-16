package org.birdview.web.binding

import org.birdview.source.SourceType
import org.birdview.web.form.CreateUserSourceFormData
import org.birdview.web.form.secret.SourceSecretFormData
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import javax.servlet.http.HttpServletRequest

class AddSourceFormArgumentResolver: HandlerMethodArgumentResolver {
    override fun supportsParameter(methodParameter: MethodParameter): Boolean =
        methodParameter.declaringClass == CreateUserSourceFormData::class.java

    override fun resolveArgument(
        methodParameter: MethodParameter,
        modelAndViewContainer: ModelAndViewContainer?,
        nativeWebRequest: NativeWebRequest,
        webDataBinderFactory: WebDataBinderFactory?
    ): Any {
        val request = nativeWebRequest.nativeRequest as HttpServletRequest
        return CreateUserSourceFormData(
            sourceName = request.getParameter("sourceName"),
            sourceUserName = request.getParameter("sourceUserName"),
            sourceType = SourceType.valueOf(request.getParameter("sourceType").toUpperCase()),
            baseUrl = request.getParameter("baseUrl"),
            sourceSecretFormData = extractSourceSecretsFormData(request)
        )
    }

    private fun extractSourceSecretsFormData(request: HttpServletRequest): SourceSecretFormData {
        throw UnsupportedOperationException()
    }
}