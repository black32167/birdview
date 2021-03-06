package org.birdview.web.secrets

import org.birdview.source.gdrive.GAccessTokenResponse
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.storage.BVGDriveConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.web.BVWebPaths
import org.birdview.web.secrets.GdriveSourceWebController.Companion.CONTROLLER_PATH
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(CONTROLLER_PATH)
class GdriveSourceWebController(
        sourceSecretsStorage: BVSourceSecretsStorage,
        httpClientFactory: BVHttpClientFactory,
        private val tokenStorage: OAuthRefreshTokenStorage,
): AbstractOauthSourceWebController<GAccessTokenResponse, BVGDriveConfig, GdriveSourceWebController.GdriveSourceFormData>(
    httpClientFactory,
    sourceSecretsStorage
) {
    companion object {
        const val CONTROLLER_PATH = "${BVWebPaths.SECRETS}/gdrive"
    }
    class GdriveSourceFormData(
            sourceName: String,
            user: String,
            val key: String?,
            val secret: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "gdrive")

    override fun consumeAuthCodeExchangeResponse(sourceName: String, rawResponse: GAccessTokenResponse) {
        val refreshToken = rawResponse.refresh_token
                ?: throw IllegalStateException("Cannot obtain refresh token from auth code exchange response")
        tokenStorage.saveRefreshToken(sourceName, refreshToken)
    }

    override fun getControllerPath() = CONTROLLER_PATH

    override fun getConfigClass() = BVGDriveConfig::class.java

    override fun mapConfig(sourceFormData: GdriveSourceFormData) =
            BVGDriveConfig (
                    sourceName = sourceFormData.sourceName,
                    clientId = sourceFormData.key!!,
                    clientSecret = sourceFormData.secret!!,
                    user = sourceFormData.user
            )

    override fun mapForm(config: BVGDriveConfig) =
            GdriveSourceFormData (
                    sourceName = config.sourceName,
                    key = config.clientId,
                    secret = config.clientSecret,
                    user = config.user)

    override fun getAuthCodeResponseClass(): Class<GAccessTokenResponse> = GAccessTokenResponse::class.java
}
