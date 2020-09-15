package org.birdview.web.source

import org.birdview.config.BVSlackConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.web.BVOAuthController
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("${BVWebPaths.SETTINGS}/slack")
class SlackSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider,
        private val oauthController: BVOAuthController
): AbstractSourceWebController<BVSlackConfig, SlackSourceWebController.SlackSourceFormData>(sourcesConfigProvider) {
    class SlackSourceFormData(
            sourceName: String,
            user: String,
            val clientId: String?,
            val clientSecret: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "slack")

    override fun getRedirectAfterSaveView(config: BVSlackConfig) =
            RedirectView(oauthController.getAuthTokenUrl(config))

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
