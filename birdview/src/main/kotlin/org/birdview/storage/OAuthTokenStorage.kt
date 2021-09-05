package org.birdview.storage

import org.birdview.storage.model.BVOAuthTokens

interface OAuthTokenStorage {
    fun loadOAuthTokens(bvUser: String, sourceName: String): BVOAuthTokens?
    fun saveOAuthTokens(bvUser: String, sourceName: String, tokens: BVOAuthTokens)
}