package org.birdview.web.secrets

import org.birdview.security.UserContext
import org.birdview.source.SourceType
import org.birdview.source.gdrive.GDriveOAuthClient
import org.birdview.source.oauth.AbstractOAuthClient
import org.birdview.source.slack.SlackOAuthClient
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.SourceSecretsMapper
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.web.BVWebPaths
import org.birdview.web.BVWebPaths.OAUTH_CODE_ENDPOINT_PATH
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import javax.ws.rs.BadRequestException

@Controller
class OAuthSourceWebController (
    private val userSourceConfigStorage: BVUserSourceConfigStorage,
    private val sourceSecretsMapper: SourceSecretsMapper,
    private val gDriveOAuthClient: GDriveOAuthClient,
    private val slackOAuthClient: SlackOAuthClient
) {
    companion object {
        fun getRedirectCodeUrl(source: String) =
            "${org.birdview.web.WebUtils.getBaseUrl()}${OAUTH_CODE_ENDPOINT_PATH}" +
                    "?source=${source}"
    }
    private val log = LoggerFactory.getLogger(OAuthSourceWebController::class.java)

    @GetMapping(OAUTH_CODE_ENDPOINT_PATH)
    fun onAuthorizationCode(
            @RequestParam(value = "source", required = true) source: String,
            @RequestParam(value = "code", required = false) code: String?,
            @RequestParam(value = "error", required = false) maybeError: String?,
            @RequestParam(value = "scope", required = false) scope: String?
    ): ModelAndView {
        when {
            maybeError != null -> log.error("OAuth authentication error for source ${source}:${maybeError}")
            code != null -> exchangeAuthorizationCodeForAccessToken(
                sourceName = source, authCode = code)
            else -> log.error("OAuth authentication error for source ${source}:no code provided!")
        }
        return ModelAndView("redirect:${BVWebPaths.ADMIN_ROOT}")
    }

    private fun exchangeAuthorizationCodeForAccessToken(
        sourceName: String, authCode:String) {
        val bvUser = UserContext.getUserName()
        val config = userSourceConfigStorage.getSource(bvUser = bvUser, sourceName = sourceName)
        val oauthSecret = config
            ?.serializedSourceSecret
            ?.let { sourceSecretsMapper.deserialize(it) }
            as? BVOAuthSourceSecret
            ?: throw BadRequestException("Inappropriate secret type")
        val oAuthClient: AbstractOAuthClient<*> = when (config.sourceType) {
            SourceType.GDRIVE -> gDriveOAuthClient
            SourceType.SLACK -> slackOAuthClient
            else -> throw BadRequestException("Unsupported source type: ${config.sourceType}")
        }

        oAuthClient.updateAccessToken(
            sourceName = sourceName,
            authCode = authCode,
            redirectUrl = getRedirectCodeUrl(sourceName),
            oauthSecret = oauthSecret)
    }
}