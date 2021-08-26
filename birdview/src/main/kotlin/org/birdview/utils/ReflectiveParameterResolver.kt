package org.birdview.utils

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import javax.ws.rs.BadRequestException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

class ReflectiveParameterResolver(
    private val stringParametersProvider: (String) -> String?
): ParameterResolver {
    override fun resolve(name: String, classifier: KClass<*>): Any? {
        return if (classifier.isSubclassOf(Enum::class)) {
            classifier.java.enumConstants
                .map { it as Enum<*> }
                .firstOrNull { it.name.equals(stringParametersProvider(name), ignoreCase = true) }
                ?: throw NoSuchElementException("The enumeration ${classifier.simpleName} does not contain element '${stringParametersProvider(name)}' for parameter '$name'")
        } else if (classifier == String::class) {
            stringParametersProvider(name)
        } else if (classifier == Boolean::class) {
            stringParametersProvider(name).toBoolean()
        } else {
            val jsonTypeInfo:JsonTypeInfo? = classifier.findAnnotation<JsonTypeInfo>()
            val jsonSubTypes:JsonSubTypes? = classifier.findAnnotation<JsonSubTypes>()
            val targetClass:KClass<*> = if (jsonTypeInfo != null && jsonSubTypes != null) {
                val classifierName:String = stringParametersProvider(jsonTypeInfo.property)
                    ?: throw IllegalArgumentException("Cannot resolve classifier parameter '${jsonTypeInfo.property}'")
                jsonSubTypes.value.find { it.name.equals(classifierName, ignoreCase = true) }?.value
                    ?: throw RuntimeException("Could not find subtype of the class '${classifier.simpleName}' " +
                            "by classifier name '$classifierName' associated with parameter '${jsonTypeInfo.property}'")
            } else {
                classifier
            }
            //jsonTypeInfo.
            ReflectiveObjectMapper.toObjectCatching(targetClass, this)
        }
            ?: throw BadRequestException("Could not resolve parameter ${name}, unsupported parameter type: ${classifier.simpleName}")
    }
}
