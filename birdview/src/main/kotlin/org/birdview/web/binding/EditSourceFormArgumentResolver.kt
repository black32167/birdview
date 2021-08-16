package org.birdview.web.binding

import org.birdview.source.SourceType
import org.birdview.web.form.UpdateUserSourceFormData
import org.birdview.web.form.secret.SourceSecretFormData
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import javax.servlet.http.HttpServletRequest

class EditSourceFormArgumentResolver: HandlerMethodArgumentResolver {
    override fun supportsParameter(methodParameter: MethodParameter): Boolean =
        methodParameter.declaringClass == UpdateUserSourceFormData::class.java

    override fun resolveArgument(
        methodParameter: MethodParameter,
        modelAndViewContainer: ModelAndViewContainer?,
        nativeWebRequest: NativeWebRequest,
        webDataBinderFactory: WebDataBinderFactory?
    ): Any {
        val request = nativeWebRequest.nativeRequest as HttpServletRequest
        return UpdateUserSourceFormData(
            enabled = request.getParameter("enabled"),
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