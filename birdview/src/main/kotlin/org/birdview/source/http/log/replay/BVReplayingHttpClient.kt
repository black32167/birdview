package org.birdview.source.http.log.replay

import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.log.HttpInteraction
import org.birdview.utils.BVConversionUtils
import org.birdview.utils.BVConversionUtils.objectToMap
import org.birdview.utils.JsonDeserializer
import java.io.FileNotFoundException
import java.lang.AssertionError
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.ws.rs.core.GenericType

class BVReplayingHttpClient(
    private val interactionsFolder: Path,
    private val jsonDeserializer: JsonDeserializer
): BVHttpClient {
    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        deserializeInteraction(resultClass, subPath, parameters)

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        deserializeInteraction(resultClass, subPath, parameters + objectToMap(postEntity))

    override fun <T> post(
        resultType: GenericType<T>,
        postEntity: Any,
        subPath: String?,
        parameters: Map<String, Any>
    ): T =
        deserializeInteraction(resultType.rawType as Class<T>, subPath, parameters + objectToMap(postEntity))

    override fun <T> postForm(resultClass: Class<T>, subPath: String?, formFields: Map<String, String>): T =
        deserializeInteraction(resultClass, subPath, formFields)

    private fun <T> deserializeInteraction(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T {
        val maybeFound = Files.list(interactionsFolder)
            .filter { it.fileName.toString().startsWith(resultClass.simpleName) }
            .map { jsonDeserializer.deserialize(it, HttpInteraction::class.java) }
            .filter { interaction->
                interaction.parameters == parameters
            }
            .findFirst()
        if (maybeFound.isEmpty) {
            throw AssertionError("Could not find logged response")
        }
        return maybeFound
            .get()
            .responsePayload
            .let { jsonDeserializer.deserializeString(it, resultClass) }
    }
}