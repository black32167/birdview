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
    fun <T> deserialize(jsonFile: Path, targetClass: Class<T>): T =
            objectMapper.readValue(jsonFile.toFile(), targetClass)
    fun serialize(jsonFile: Path, payload: Any) =
            objectMapper.writeValue(jsonFile.toFile(), payload)
    inline fun <reified T> deserialize(jsonFile: Path): T =
            deserialize(jsonFile, T::class.java)
}