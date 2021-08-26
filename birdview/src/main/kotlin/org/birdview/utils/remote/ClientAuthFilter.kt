package org.birdview.utils.remote

import java.util.*
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientRequestFilter
import javax.ws.rs.core.UriBuilder

class ClientAuthFilter(val authProvider:()-> ApiAuth?): ClientRequestFilter {
    override fun filter(requestContext: ClientRequestContext) {
        val auth: ApiAuth? = authProvider()
        auth?.also {
            when(it) {
                is BearerAuth -> requestContext.headers.add("Authorization", "Bearer ${it.bearerToken}")
                is BasicAuth -> basic(requestContext, it)
                is ParameterAuth -> parameter(requestContext, it)
            }
        }
    }

    private fun parameter(requestContext: ClientRequestContext, auth: ParameterAuth) {
        requestContext.setUri(UriBuilder.fromUri(requestContext.getUri())
            .queryParam("key", auth.key)
            .queryParam("token", auth.token)
            .build());
    }

    private fun basic(requestContext: ClientRequestContext, auth: BasicAuth) {
        val urf8Bytes = "${auth.user}:${auth.password}".toByteArray()
        val basicToken = Base64.getEncoder().encodeToString(urf8Bytes)
        requestContext.headers.add("Authorization", "Basic ${basicToken}")
    }
}