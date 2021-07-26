package org.birdview.storage

import org.birdview.storage.model.BVOAuthTokens

interface OAuthTokenStorage {
    fun loadOAuthTokens(sourceName: String): BVOAuthTokens?
    fun saveOAuthTokens(sourceName: String, tokens: BVOAuthTokens)
}