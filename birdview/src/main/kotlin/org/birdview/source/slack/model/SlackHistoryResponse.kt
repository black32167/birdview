package org.birdview.source.slack.model

//@JsonTypeInfo(
//        use = JsonTypeInfo.Id.CUSTOM,
//        include = JsonTypeInfo.As.PROPERTY,
//        property = "ok")
//@JsonSubTypes(
//        JsonSubTypes.Type(value = SlackHistoryResponse::class, name = "jira"),
//        JsonSubTypes.Type(value = SlackErrorResponse::class, name = "trello")
//)
class SlackHistoryResponse (
        ok: Boolean,
        error: String?,
        val messages: List<SlackMessage>?,
        val hasMore: Boolean?,
        val pin_count: Integer?,
        val response_metadata: SlackResponseMetadata?
): AbstractSlackResponse(ok, error)