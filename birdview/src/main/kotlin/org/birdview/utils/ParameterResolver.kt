package org.birdview.utils

import kotlin.reflect.KClass

@FunctionalInterface
interface ParameterResolver {
    fun <T: Any> resolve(name: String, classifier: KClass<T>): T?
}