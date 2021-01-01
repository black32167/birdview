package org.birdview.source.http.log

class HttpInteraction (
    val endpointUrl: String?,
    val resultType: String,
    val parameters: Map<String, Any>,
    val responsePayload: String
)