package org.birdview.storage.model

class BVOAuthTokens (
    val accessToken:String = "",
    val refreshToken:String? = null,
    val expiresTimestamp:Long? = null
) {
    fun isExpired() = expiresTimestamp?:0 < System.currentTimeMillis()
}