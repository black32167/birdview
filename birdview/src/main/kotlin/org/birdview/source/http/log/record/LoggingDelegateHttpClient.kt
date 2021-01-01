package org.birdview.source.http.log.record

import com.fasterxml.jackson.databind.ObjectMapper
import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.log.HttpInteraction
import org.birdview.utils.JsonDeserializer
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.ws.rs.core.GenericType

class LoggingDelegateHttpClient(
    private val delegate: BVHttpClient,
    private val outputFolder: Path,
    private val jsonDeserializer: JsonDeserializer
): BVHttpClient {
    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        delegate.get(resultClass, subPath, parameters).also { serialize(
            HttpInteraction(
            endpointUrl = subPath,
            resultType = resultClass.simpleName,
            parameters = parameters,
            responsePayload = serializePayload(it)
        )
        ) }

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        delegate.post(resultClass, postEntity, subPath, parameters).also { serialize(
            HttpInteraction(
            endpointUrl = subPath,
            resultType = resultClass.simpleName,
            parameters = parameters,
            responsePayload = serializePayload(it)
        )
        ) }

    override fun <T> post(
        resultType: GenericType<T>,
        postEntity: Any,
        subPath: String?,
        parameters: Map<String, Any>
    ): T = delegate.post(resultType, postEntity, subPath, parameters).also { serialize(
        HttpInteraction(
        endpointUrl = subPath,
        resultType = resultType.rawType.simpleName,
        parameters = parameters,
        responsePayload = serializePayload(it)
    )
    ) }

    override fun <T> postForm(
        resultClass: Class<T>,
        subPath: String?,
        formFields: Map<String, String>
    ): T = delegate.postForm(resultClass, subPath, formFields).also { serialize(
        HttpInteraction(
        endpointUrl = subPath,
        resultType = resultClass.simpleName,
        parameters = formFields,
        responsePayload = serializePayload(it)
    )
    ) }

    private fun serialize(interaction: HttpInteraction) {
        jsonDeserializer.serialize(
            outputFolder.resolve("${interaction.resultType}-${UUID.randomUUID()}.json"),
            interaction)
    }

    private fun <T> serializePayload(payload: T): String =
        jsonDeserializer.serializetoString(payload)
}
