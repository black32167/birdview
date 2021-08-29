package org.birdview.utils

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import javax.ws.rs.BadRequestException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

class ReflectiveParameterResolver(
    private val underlyingParametersProvider: ParameterResolver
): ParameterResolver {
    override fun <T: Any> resolve(name: String, classifier: KClass<T>): T? {
        val underlyingResolution = underlyingParametersProvider.resolve(name, classifier)
        if (underlyingResolution != null) {
            return underlyingResolution
        }

        return if (classifier.isSubclassOf(Enum::class)) {
            val targetElementName = resolveString(name)
            classifier.java.enumConstants
                .map { it as Enum<*> }
                .firstOrNull { it.name.equals(targetElementName, ignoreCase = true) } as T?
                ?: throw NoSuchElementException("The enumeration ${classifier.simpleName} does not contain element '${targetElementName}' for parameter '$name'")
        } else if (classifier == Boolean::class) {
            resolveString(name)?.toBoolean()
        } else {
            val jsonTypeInfo:JsonTypeInfo? = classifier.findAnnotation<JsonTypeInfo>()
            val jsonSubTypes:JsonSubTypes? = classifier.findAnnotation<JsonSubTypes>()
            val targetClass:KClass<*> = if (jsonTypeInfo != null && jsonSubTypes != null) {
                val classifierName:String = underlyingParametersProvider.resolve(jsonTypeInfo.property, String::class)
                    ?: throw IllegalArgumentException("Cannot resolve classifier parameter '${jsonTypeInfo.property}'")
                jsonSubTypes.value.find { it.name.equals(classifierName, ignoreCase = true) }?.value
                    ?: throw RuntimeException("Could not find subtype of the class '${classifier.simpleName}' " +
                            "by classifier name '$classifierName' associated with parameter '${jsonTypeInfo.property}'")
            } else {
                classifier
            }
            //jsonTypeInfo.
            ReflectiveObjectMapper.toObjectCatching(targetClass, this)
        } as T?
            ?: throw BadRequestException("Could not resolve parameter ${name}, unsupported parameter type: ${classifier.simpleName}")
    }

    private fun resolveString(name:String):String? =
        underlyingParametersProvider.resolve(name, String::class)
}
