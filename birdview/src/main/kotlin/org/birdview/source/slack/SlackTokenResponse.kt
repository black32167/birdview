package org.birdview.source.slack

class SlackTokenResponse (
        val ok: Boolean,
        val error: String?,
        val access_token: String?,
        val authed_user: SlackAuthorizedUser?
)

class SlackAuthorizedUser (
        val id: String,
        val scope:String,
        val access_token: String?,
        val token_type: String
)
