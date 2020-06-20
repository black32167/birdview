package org.birdview.utils.remote

import java.util.*
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientRequestFilter

class ClientAuthFilter(val authProvider:()-> ApiAuth?): ClientRequestFilter {
    override fun filter(requestContext: ClientRequestContext) {
        val auth: ApiAuth? = authProvider()
        auth?.also {
            when(it) {
                is BearerAuth -> requestContext.headers.add("Authorization", "Bearer ${it.bearerToken}")
                is BasicAuth -> basic(requestContext, it)
            }
        }
    }

    private fun basic(requestContext: ClientRequestContext, auth: BasicAuth) {
        val urf8Bytes = "${auth.user}:${auth.password}".toByteArray()
        val basicToken = Base64.getEncoder().encodeToString(urf8Bytes)
        requestContext.headers.add("Authorization", "Basic ${basicToken}")
    }
}