package org.birdview.utils

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

object ReflectiveObjectMapper {
    private val log = LoggerFactory.getLogger(ReflectiveObjectMapper::class.java)

    fun <T: Any> toObjectCatching(targetClass: KClass<T>, parameterResolver: ParameterResolver): T? {
        val constructor: KFunction<*> = targetClass.constructors
            .filter { it.parameters.isNotEmpty() }
            .takeIf { it.size == 1 }
            ?.first()
            ?: throw IllegalArgumentException("Exactly one constructor with arguments is expected in class ${targetClass}")

        val constructorParameterValues = constructor.parameters
            .filter { it.name != null }
            .map { it to run {
                val classifier = (it.type.classifier as KClass<*>)
                parameterResolver.resolve(it.name!!, classifier)
            }}
            .toMap()

        try {
            return constructor.callBy(constructorParameterValues) as T?
        } catch (e:Exception) {
            log.error("", e)
            return null
        }
    }
}