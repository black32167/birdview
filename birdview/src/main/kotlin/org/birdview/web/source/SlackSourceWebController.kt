package org.birdview.web.source

import org.birdview.config.sources.BVSlackConfig
import org.birdview.config.sources.BVSourcesConfigStorage
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.source.slack.SlackTokenResponse
import org.birdview.web.BVWebPaths
import org.birdview.web.source.SlackSourceWebController.Companion.CONTROLLER_PATH
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.ws.rs.core.Response

@Controller
@RequestMapping(CONTROLLER_PATH)
class SlackSourceWebController(
        sourcesConfigStorage: BVSourcesConfigStorage,
        private val tokenStorage: OAuthRefreshTokenStorage
): AbstractOauthSourceWebController<BVSlackConfig, SlackSourceWebController.SlackSourceFormData>(sourcesConfigStorage) {
    companion object {
        const val CONTROLLER_PATH = "${BVWebPaths.SECRETS}/slack"
    }

    class SlackSourceFormData(
            sourceName: String,
            user: String,
            val clientId: String?,
            val clientSecret: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "slack")


    override fun consumeAuthCodeExchangeResponse(sourceName: String, rawResponse: Response) {
        val slackTokenResponse = rawResponse.readEntity(SlackTokenResponse::class.java)
        val userAccessToken = slackTokenResponse.authed_user
                ?.access_token
                ?: throw IllegalStateException("Cannot obtain refresh token from auth code exchange response")
        tokenStorage.saveAccessToken(sourceName, userAccessToken)
    }

    override fun getControllerPath() = CONTROLLER_PATH

    override fun getConfigClass() = BVSlackConfig::class.java

    override fun mapConfig(sourceFormData: SlackSourceFormData) =
            BVSlackConfig (
                    sourceName = sourceFormData.sourceName,
                    clientId = sourceFormData.clientId!!,
                    clientSecret = sourceFormData.clientSecret!!,
                    user = sourceFormData.user
            )

    override fun mapForm(config: BVSlackConfig) =
            SlackSourceFormData (
                    sourceName = config.sourceName,
                    clientId = config.clientId,
                    clientSecret = config.clientSecret,
                    user = config.user)
}
