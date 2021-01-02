package org.birdview.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.file.Path
import javax.inject.Named

@Named
class JsonDeserializer {
    private val objectMapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(KotlinModule())
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
}
