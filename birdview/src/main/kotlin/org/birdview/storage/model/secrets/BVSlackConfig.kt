package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

class BVSlackConfig (
        sourceName: String = "slack",
        user: String,
        clientId: String,
        clientSecret: String
): BVOAuthSourceConfig(
        sourceType = SourceType.SLACK,
        sourceName = sourceName,
        user = user,
        clientId = clientId,
        clientSecret = clientSecret,
        authCodeUrl = "https://slack.com/oauth/v2/authorize?user_scope=identity.basic&", //search:read
        tokenExchangeUrl = "https://slack.com/api/oauth.v2.access",
        scope = "channels:history,channels:read"
) {
    val baseUrl = "https://slack.com/api"
}