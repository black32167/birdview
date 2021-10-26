package org.birdview.source.http.log.record

import org.birdview.source.http.BVHttpClient
import org.birdview.source.http.log.HttpInteraction
import org.birdview.utils.JsonMapper
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class LoggingDelegateHttpClient(
    private val delegate: BVHttpClient,
    private val outputFolder: Path,
    private val jsonMapper: JsonMapper
): BVHttpClient {
    override val basePath: String
        get() = delegate.basePath

    override fun <T> get(resultClass: Class<T>, subPath: String?, parameters: Map<String, Any>): T =
        delegate.get(String::class.java, subPath, parameters).also { payload: String ->
            serialize(
                endpointUrl = subPath,
                resultType = resultClass.simpleName,
                parameters = parameters,
                responsePayload = payload
            )
        }
            .let { payload: String ->
                jsonMapper.deserializeString(payload, resultClass)
            }

    override fun <T> post(resultClass: Class<T>, postEntity: Any, subPath: String?, parameters: Map<String, Any>): T =
        delegate.post(String::class.java, postEntity, subPath, parameters).also { payload: String ->
            serialize(
                endpointUrl = subPath,
                resultType = resultClass.simpleName,
                parameters = parameters + jsonMapper.objectToMap(postEntity),
                responsePayload = payload
            )
        }
            .let { payload: String ->
                jsonMapper.deserializeString(payload, resultClass)
            }


    override fun <T> postForm(
        resultClass: Class<T>,
        subPath: String?,
        formFields: Map<String, String>
    ): T = delegate.postForm(String::class.java, subPath, formFields).also { payload: String ->
        serialize(
            endpointUrl = subPath,
            resultType = resultClass.simpleName,
            parameters = formFields,
            responsePayload = payload
        )
    }
        .let { payload: String ->
            jsonMapper.deserializeString(payload, resultClass)
        }

    private fun serialize(
        endpointUrl: String?,
        resultType: String,
        parameters: Map<String, Any>,
        responsePayload: String
    ) {
        serialize(
            HttpInteraction(
                endpointUrl = "$basePath/$endpointUrl",
                resultType = resultType,
                parameters = parameters,
                responsePayload = responsePayload
            )
        )
    }

    private fun serialize(interaction: HttpInteraction) {
        val responseSuffix = UUID.randomUUID().toString()
        val outputFile = outputFolder.resolve("${interaction.resultType}-${responseSuffix}.json")
        if (Files.exists(outputFile)) {
            throw IllegalStateException("File ${outputFile} already exists")
        }
        jsonMapper.serialize(
            outputFile,
            interaction
        )
    }
}
