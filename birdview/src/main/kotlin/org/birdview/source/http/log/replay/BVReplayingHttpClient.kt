package org.birdview.source.http.log.replay

import org.apache.juli.logging.LogFactory
import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.log.HttpInteraction
import org.birdview.utils.JsonDeserializer
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class BVReplayingHttpClient(
    private val interactionsFolder: Path,
    private val jsonDeserializer: JsonDeserializer
): BVHttpClient {
    private val log = LogFactory.getLog(BVReplayingHttpClient::class.java)

    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        deserializeInteraction(resultClass, subPath, parameters)

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        deserializeInteraction(resultClass, subPath, parameters + jsonDeserializer.objectToMap(postEntity))

    override fun <T> postForm(resultClass: Class<T>, subPath: String?, formFields: Map<String, String>): T =
        deserializeInteraction(resultClass, subPath, formFields)

    private fun <T> findInteraction(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): HttpInteraction {
        val found = Files.list(interactionsFolder)
            .filter { it.fileName.toString().startsWith(resultClass.simpleName) }
            .map { jsonDeserializer.deserialize(it, HttpInteraction::class.java) }
            .filter { interaction->
                interaction.parameters == parameters && subPath == interaction.endpointUrl
            }
            .collect(Collectors.toList())
        if (found.isEmpty()) {
            throw AssertionError("Could not find logged response")
        }
        if (found.size != 1) {
            log.warn("Multiple responses match")
        }
        return found.first()
    }

    private fun <T> deserializeInteraction(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T {
        return findInteraction(resultClass, subPath, parameters)
            .responsePayload
            .let {
                jsonDeserializer.deserializeString(it, resultClass)
            }
    }
}
