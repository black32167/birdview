package org.birdview.utils.remote

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.ClientProperties
import java.net.URI
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget

class WebTargetFactory(url:String, val authProvider: () -> ApiAuth? = {null}) {
    private val timeoutMs = 1000000
    private val client = buildClient()

    private fun buildClient(): Client =
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
        )

    private val baseTarget = client.target(URI.create(url))

    fun getTarget(subPath: String): WebTarget {
        return baseTarget.path(subPath)
    }
}