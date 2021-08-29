package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentSnapshot
import org.birdview.utils.ParameterResolver
import org.birdview.utils.ReflectiveObjectMapper
import org.birdview.utils.ReflectiveParameterResolver
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object DocumentObjectMapper {
    private val log = LoggerFactory.getLogger(DocumentObjectMapper::class.java)
    private val supportedPrimitives:List<KClass<*>> = listOf(String::class, Boolean::class, List::class)

    fun <T: Any> toObjectCatching(doc: DocumentSnapshot, targetClass: KClass<T>): T? {
        val undelyingResolver = object:ParameterResolver {
            override fun <T : Any> resolve(name: String, classifier: KClass<T>): T? =
                if (supportedPrimitives.contains(classifier)) {
                    doc.get(name) as T?
                } else {
                    null
                }
        }

        return ReflectiveObjectMapper.toObjectCatching(targetClass, ReflectiveParameterResolver(undelyingResolver))
    }
}