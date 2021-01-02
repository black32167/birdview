package org.birdview.utils

object BVConversionUtils {
    fun objectToMap(obj: Any): Map<String, Any> =
        obj::class.java.declaredFields
            .map { it.isAccessible = true; it.name to it.get(obj) }
            .filter { it.second != null }
            .toMap()
}