package org.birdview.storage.model.secrets

import org.birdview.source.SourceType

abstract class BVOAuthSourceConfig (
    sourceType: SourceType,
    sourceName: String,
    user: String,
    val clientId: String,
    val clientSecret: String,
    val authCodeUrl: String,
    val tokenExchangeUrl: String,
    val scope: String)
    : BVAbstractSourceConfig(sourceType, sourceName, user)