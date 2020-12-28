package org.birdview.source.response.log.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import org.birdview.analysis.BVDocument
import org.birdview.source.SourceType

class SourceInteraction(
    val sourceName: String,
    val sourceType: SourceType,
    val request: SourceRequest,
    val response: List<BVDocument>
)

@JsonSubTypes(
    JsonSubTypes.Type(value = TimeIntervalSourceRequest::class, name = "TimeIntervalSourceRequest"),
    JsonSubTypes.Type(value = IdsSourceRequest::class, name = "IdsSourceRequest")
)
abstract class SourceRequest

class TimeIntervalSourceRequest(
    val bvUser: String,
    val fromTime: String?,
    val toTime: String?
): SourceRequest()

class IdsSourceRequest(
    idsList: List<String>
): SourceRequest ()