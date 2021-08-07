package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

class BVGDriveSecret (
        sourceName: String = "gdrive",
        clientId: String,
        clientSecret: String,
        user: String
): BVOAuthSourceSecret(
        sourceType = SourceType.GDRIVE,
        sourceName = sourceName,
        user = user,
        clientId = clientId,
        clientSecret = clientSecret,
        authCodeUrl = "https://accounts.google.com/o/oauth2/v2/auth?",
        tokenExchangeUrl = "https://oauth2.googleapis.com/token",
        scope = "https://www.googleapis.com/auth/drive"
)