package org.birdview.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.file.Path
import javax.inject.Named

@Named
class JsonMapper {
    private val objectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(KotlinModule())
        .registerModule(JavaTimeModule())
    fun <T> deserialize(jsonFile: Path, targetClass: Class<T>): T = try {
        objectMapper.readValue(jsonFile.toFile(), targetClass)
    } catch (e: Exception) {
        throw RuntimeException("Error deserializing $jsonFile", e)
    }

    fun <T> deserializeString(payload: String, resultClass: Class<T>): T = try {
        objectMapper.readValue(payload, resultClass)
    } catch (e: Exception) {
        throw RuntimeException("Error deserializing from string to ${resultClass.simpleName}", e)
    }

    fun serialize(jsonFile: Path, payload: Any) =
            objectMapper.writeValue(jsonFile.toFile(), payload)

    inline fun <reified T> deserialize(jsonFile: Path): T =
            deserialize(jsonFile, T::class.java)

    fun <T> serializeToString(payload: T): String =
        objectMapper.writeValueAsString(payload)

    fun objectToMap(obj: Any): Map<String, Any> {
        val serialized = objectMapper.writeValueAsString(obj)
        return objectMapper.readValue(serialized, object: TypeReference<Map<String, Any>>() {})
    }
}
