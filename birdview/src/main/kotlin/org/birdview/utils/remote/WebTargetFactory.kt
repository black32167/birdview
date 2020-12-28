package org.birdview.utils.remote

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.logging.LoggingFeature
import java.net.URI
import java.util.logging.Level
import java.util.logging.Logger
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget

class WebTargetFactory(url:String, enableLogging:Boolean = false, val authProvider: () -> ApiAuth? = {null}) {
    private val timeoutMs = 1000000
    private val client = buildClient(enableLogging)

    private fun buildClient(enableLogging: Boolean): Client =
        ClientBuilder.newClient(
                ClientConfig(ClientAuthFilter(authProvider))
                        .property("jersey.config.jsonFeature", "disabled")
                        .property(ClientProperties.CONNECT_TIMEOUT, timeoutMs)
                        .property(ClientProperties.READ_TIMEOUT, timeoutMs)
                        .property("http.connection.timeout", timeoutMs)
                        .property("http.receive.timeout", timeoutMs)
                        .register(JacksonJsonProvider(ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                .registerModule(KotlinModule())))
                        .also { if (enableLogging) {
                            it.register(LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
                                    Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT, Integer.MAX_VALUE/2))
                        }}
        )

    private val baseTarget = client.target(URI.create(url))

    fun getTarget(subPath: String? = null): WebTarget {
        return if (subPath != null) {
            baseTarget.path(subPath)
        } else {
            baseTarget
        }
    }
}
