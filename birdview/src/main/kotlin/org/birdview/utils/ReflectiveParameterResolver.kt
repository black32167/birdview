package org.birdview.utils

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import javax.ws.rs.BadRequestException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

class ReflectiveParameterResolver(
    private val stringParametersProvider: (String) -> String
): ParameterResolver {
    override fun resolve(name: String, classifier: KClass<*>): Any? {
        return if (classifier.isSubclassOf(Enum::class)) {
            classifier.java.enumConstants
                .map { it as Enum<*> }
                .first { it.name == stringParametersProvider(name) }
        } else if (classifier == String::class) {
            stringParametersProvider(name)
        } else if (classifier == Boolean::class) {
            stringParametersProvider(name).toBoolean()
        } else {
            val jsonTypeInfo:JsonTypeInfo? = classifier.findAnnotation<JsonTypeInfo>()
            val jsonSubTypes:JsonSubTypes? = classifier.findAnnotation<JsonSubTypes>()
            val targetClass:KClass<*> = if (jsonTypeInfo != null && jsonSubTypes != null) {
                val classifierName:String = stringParametersProvider(jsonTypeInfo.property)
                jsonSubTypes.value.find { it.name.toLowerCase() == classifierName.toLowerCase() }?.value
                    ?: throw RuntimeException("Could not find subtype by classifier name: ${classifier}")
            } else {
                classifier
            }
            //jsonTypeInfo.
            ReflectiveObjectMapper.toObjectCatching(targetClass, this)
        }
            ?: throw BadRequestException("Could not resolve parameter ${name}, unsupported parameter type: ${classifier.simpleName}")
    }
}
