package org.birdview.source.slack.model

class SlackMessage (
        val type: String,
        val user: String,
        val text: String,
        val ts: String
)