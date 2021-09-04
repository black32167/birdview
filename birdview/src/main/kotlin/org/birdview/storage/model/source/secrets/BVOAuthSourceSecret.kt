package org.birdview.storage.model.source.secrets

class BVOAuthSourceSecret (
    val sourceName:String,
    val flavor: OAuthFlavour,
    val clientId: String,
    val clientSecret: String,
    val authCodeUrl: String,
    val tokenExchangeUrl: String,
    val scope: String)
    : BVSourceSecret {
        enum class OAuthFlavour {
            GDRIVE, SLACK
        }
    }
