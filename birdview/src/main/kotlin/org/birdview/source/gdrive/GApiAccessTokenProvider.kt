package org.birdview.source.gdrive

import org.birdview.config.BVGDriveConfig
import org.birdview.utils.remote.WebTargetFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.inject.Named
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form

@Named
class GApiAccessTokenProvider(
        val authorizationCodeProvider: GApiAuthorizationCodeProvider) {
    companion object {
        private const val tokenExchangeUrl = "https://oauth2.googleapis.com/token"
        private val refreshTokenFile = Paths.get("/tmp/birdview/gapir")
    }

    fun getToken(config: BVGDriveConfig, scope:String): String? = loadLocalRefreshToken()
            ?.let { refreshToken-> getRemoteAccessToken(refreshToken, config) }
            ?: getRemoteAccessToken(config, scope)

    private fun getRemoteAccessToken(config: BVGDriveConfig, scope:String): String =
            authorizationCodeProvider.getAuthCode(config, scope)
                    ?.let { authCode -> getTokensResponse(authCode, config) }
                    ?.also { it.refresh_token?.also(this::saveRefreshToken) }
                    ?.access_token
                    ?: throw java.lang.RuntimeException("No refresh token is returned")

    private fun saveRefreshToken(refreshToken:String) {
            Files.createDirectories(refreshTokenFile.parent)
            Files.write(refreshTokenFile, listOf(refreshToken),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun loadLocalRefreshToken():String? =
            if(Files.exists(refreshTokenFile))
                Files.readAllLines(refreshTokenFile).firstOrNull()
            else null

    private fun getTokensResponse(authCode: String, config: BVGDriveConfig): GAccessTokenResponse? =
        WebTargetFactory(tokenExchangeUrl)
                .getTarget("")
                .request()
                .post(getTokenExchangeFormEntity(authCode, config))
                .also { response ->
                    if(response.status != 200) {
                        throw RuntimeException("Error reading Google access token: ${response.readEntity(String::class.java)}")
                    }
                }
                .readEntity(GAccessTokenResponse::class.java)

    private fun getRemoteAccessToken(refreshToken: String, config: BVGDriveConfig): String =
            WebTargetFactory(tokenExchangeUrl)
                    .getTarget("")
                    .request()
                    .post(getTokenRefreshFormEntity(refreshToken, config))
                    .also { response ->
                        if(response.status != 200) {
                            throw RuntimeException("Error reading Google access token: ${response.readEntity(String::class.java)}")
                        }
                    }
                    .readEntity(GAccessTokenResponse::class.java)
                    .access_token

    private fun getTokenExchangeFormEntity(authCode:String, config: BVGDriveConfig) =
        Entity.form(Form()
                .param("client_id", config.clientId)
                .param("client_secret", config.clientSecret)
                .param("access_type", "offline")
                .param("code", authCode)
                .param("grant_type", "authorization_code")
                .param("redirect_uri", config.redirectUri))

    private fun getTokenRefreshFormEntity(refreshToken:String, config: BVGDriveConfig) =
            Entity.form(Form()
                    .param("client_id", config.clientId)
                    .param("client_secret", config.clientSecret)
                    .param("grant_type", "refresh_token")
                    .param("refresh_token", refreshToken))
}