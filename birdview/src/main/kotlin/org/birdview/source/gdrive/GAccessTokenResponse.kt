package org.birdview.source.gdrive

import org.birdview.source.oauth.OAuthTokenResponse

class GAccessTokenResponse (
        val access_token: String,
        val refresh_token: String?,
        val expires_in: Int?,
        val id_token: String?,
        val scope: String,
        val token_type: String
): OAuthTokenResponse
