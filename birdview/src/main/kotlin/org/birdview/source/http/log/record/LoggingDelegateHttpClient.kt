package org.birdview.source.http.log.record

import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.log.HttpInteraction
import org.birdview.utils.JsonDeserializer
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class LoggingDelegateHttpClient(
    private val delegate: BVHttpClient,
    private val outputFolder: Path,
    private val jsonDeserializer: JsonDeserializer
): BVHttpClient {
    private val resultType2Counter: MutableMap<String, AtomicInteger> = ConcurrentHashMap()

    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        delegate.get(String::class.java, subPath, parameters).also { payload: String ->
            serialize( HttpInteraction(
                endpointUrl = subPath,
                resultType = resultClass.simpleName,
                parameters = parameters,
                responsePayload = payload
            )) }
            .let { payload: String ->
                jsonDeserializer.deserializeString(payload, resultClass)
            }

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        delegate.post(String::class.java, postEntity, subPath, parameters).also { payload: String ->
            serialize( HttpInteraction(
                endpointUrl = subPath,
                resultType = resultClass.simpleName,
                parameters = parameters + jsonDeserializer.objectToMap(postEntity),
                responsePayload = payload
            )) }
            .let { payload: String ->
                jsonDeserializer.deserializeString(payload, resultClass)
            }


    override fun <T> postForm(
        resultClass: Class<T>,
        subPath: String?,
        formFields: Map<String, String>
    ): T = delegate.postForm(String::class.java, subPath, formFields).also { payload: String ->
        serialize( HttpInteraction(
            endpointUrl = subPath,
            resultType = resultClass.simpleName,
            parameters = formFields,
            responsePayload = payload
        )) }
        .let { payload: String ->
            jsonDeserializer.deserializeString(payload, resultClass)
        }

    private fun serialize(interaction: HttpInteraction) {
        val responseSuffix = UUID.randomUUID().toString()
        val outputFile = outputFolder.resolve("${interaction.resultType}-${responseSuffix}.json")
        if (Files.exists(outputFile)) {
            throw IllegalStateException("File ${outputFile} already exists")
        }
        jsonDeserializer.serialize(
            outputFile,
            interaction)
    }

    private fun serializePayload(payload: Any?): String =
        jsonDeserializer.serializeToString(payload)

}
