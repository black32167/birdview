package org.birdview.source.slack.model

class SlackErrorResponse (
        ok: Boolean,
        error: String?
) : AbstractSlackResponse(ok, error)