package org.birdview.source.slack.model

abstract class AbstractSlackResponse (
        val ok: Boolean,
        val error: String?
)