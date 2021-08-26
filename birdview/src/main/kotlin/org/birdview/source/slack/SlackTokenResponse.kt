package org.birdview.source.slack

import org.birdview.source.oauth.OAuthTokenResponse

class SlackTokenResponse (
        val ok: Boolean,
        val error: String?,
        val access_token: String?,
        val authed_user: SlackAuthorizedUser?
): OAuthTokenResponse

class SlackAuthorizedUser (
        val id: String,
        val scope:String,
        val access_token: String?,
        val token_type: String
)
