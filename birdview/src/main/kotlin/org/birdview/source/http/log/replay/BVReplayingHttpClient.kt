package org.birdview.source.http.log.replay

import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.log.HttpInteraction
import org.birdview.utils.JsonDeserializer
import java.nio.file.Files
import java.nio.file.Path
import javax.ws.rs.core.GenericType

class BVReplayingHttpClient(
    private val interactionsFolder: Path,
    private val jsonDeserializer: JsonDeserializer
): BVHttpClient {
    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        deserializeInteraction(resultClass, subPath, parameters)

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        deserializeInteraction(resultClass, subPath, parameters)

    override fun <T> post(
        resultType: GenericType<T>,
        postEntity: Any,
        subPath: String?,
        parameters: Map<String, Any>
    ): T =
        deserializeInteraction(resultType.rawType as Class<T>, subPath, parameters)

    override fun <T> postForm(resultClass: Class<T>, subPath: String?, formFields: Map<String, String>): T =
        deserializeInteraction(resultClass, subPath, formFields)

    private fun <T> deserializeInteraction(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        Files.list(interactionsFolder)
            .filter { it.fileName.toString().startsWith(resultClass.simpleName)}
            .map { jsonDeserializer.deserialize(it, HttpInteraction::class.java) }
            .findFirst()
            .get()
            .responsePayload
            .let { jsonDeserializer.deserializeString(it, resultClass) }
}