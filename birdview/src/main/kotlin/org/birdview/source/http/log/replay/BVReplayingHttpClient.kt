package org.birdview.source.http.log.replay

import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.log.HttpInteraction
import org.birdview.utils.JsonMapper
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class BVReplayingHttpClient(
    override val basePath: String,
    private val interactionsFolder: Path,
    private val jsonMapper: JsonMapper
): BVHttpClient {
    private val log = LoggerFactory.getLogger(BVReplayingHttpClient::class.java)

    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        deserializeInteraction(resultClass, subPath, parameters)

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        deserializeInteraction(resultClass, subPath, parameters + jsonMapper.objectToMap(postEntity))

    override fun <T> postForm(resultClass: Class<T>, subPath: String?, formFields: Map<String, String>): T =
        deserializeInteraction(resultClass, subPath, formFields)

    private fun <T> findInteraction(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): HttpInteraction {
        val found = Files.list(interactionsFolder)
            .filter { it.fileName.toString().startsWith(resultClass.simpleName) }
            .map { jsonMapper.deserialize(it, HttpInteraction::class.java) }
            .filter { interaction->
                interaction.parameters == parameters && "${basePath}/${subPath}" == interaction.endpointUrl
            }
            .collect(Collectors.toList())
        if (found.isEmpty()) {
            throw AssertionError("Could not find logged response ${resultClass.simpleName} (path='${subPath}',parameters=${parameters})")
        }
        if (found.size != 1) {
            log.warn("Multiple responses ({}) match for type '{}' and subpath '{}'", found.size, resultClass.simpleName, subPath)
        }
        return found.first()
    }

    private fun <T> deserializeInteraction(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T {
        return findInteraction(resultClass, subPath, parameters)
            .responsePayload
            .let {
                jsonMapper.deserializeString(it, resultClass)
            }
    }
}
