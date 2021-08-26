package org.birdview.storage.firebase

import com.google.cloud.firestore.DocumentSnapshot
import org.birdview.utils.ReflectiveObjectMapper
import org.birdview.utils.ReflectiveParameterResolver
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object DocumentObjectMapper {
    private val log = LoggerFactory.getLogger(DocumentObjectMapper::class.java)

    fun <T: Any> toObjectCatching(doc: DocumentSnapshot, targetClass: KClass<T>): T? {
        return ReflectiveObjectMapper.toObjectCatching(targetClass, ReflectiveParameterResolver { name ->
            doc.get(name).toString()
        })
    }
}