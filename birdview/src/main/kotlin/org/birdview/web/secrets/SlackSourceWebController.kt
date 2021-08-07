package org.birdview.web.secrets

import org.birdview.source.http.BVHttpClientFactory
import org.birdview.source.slack.SlackOAuthClient
import org.birdview.source.slack.SlackTokenResponse
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.model.secrets.BVSlackSecret
import org.birdview.web.BVWebPaths
import org.birdview.web.secrets.SlackSourceWebController.Companion.CONTROLLER_PATH
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(CONTROLLER_PATH)
class SlackSourceWebController(
    sourceSecretsStorage: BVSourceSecretsStorage,
    httpClientFactory: BVHttpClientFactory,
    private val oauthClient: SlackOAuthClient
): AbstractOauthSourceWebController<SlackTokenResponse, BVSlackSecret, SlackSourceWebController.SlackSourceFormData>(
    httpClientFactory,
    sourceSecretsStorage
) {
    companion object {
        const val CONTROLLER_PATH = "${BVWebPaths.SECRETS}/slack"
    }

    class SlackSourceFormData(
            sourceName: String,
            user: String,
            val clientId: String?,
            val clientSecret: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "slack")

    override fun consumeAuthCodeExchangeResponse(sourceName: String, slackTokenResponse: SlackTokenResponse) {
        oauthClient.saveOAuthTokens(sourceName, slackTokenResponse)
    }

    override fun getControllerPath() = CONTROLLER_PATH

    override fun getConfigClass() = BVSlackSecret::class.java

    override fun mapConfig(sourceFormData: SlackSourceFormData) =
            BVSlackSecret (
                    sourceName = sourceFormData.sourceName,
                    clientId = sourceFormData.clientId!!,
                    clientSecret = sourceFormData.clientSecret!!,
                    user = sourceFormData.user
            )

    override fun mapForm(config: BVSlackSecret) =
            SlackSourceFormData (
                    sourceName = config.sourceName,
                    clientId = config.clientId,
                    clientSecret = config.clientSecret,
                    user = config.user)

    override fun getAuthCodeResponseClass(): Class<SlackTokenResponse> = SlackTokenResponse::class.java
}
