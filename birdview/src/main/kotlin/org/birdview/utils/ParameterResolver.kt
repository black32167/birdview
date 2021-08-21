package org.birdview.utils

import kotlin.reflect.KClass

interface ParameterResolver {
    fun resolve(name: String, classifier: KClass<*>): Any?
}