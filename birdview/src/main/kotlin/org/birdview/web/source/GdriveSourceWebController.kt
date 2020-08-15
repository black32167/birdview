package org.birdview.web.source

import org.birdview.config.BVGDriveConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.web.BVOAuthController
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("${BVWebPaths.SETTINGS}/gdrive")
class GdriveSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider,
        private val oauthController: BVOAuthController
): AbstractSourceWebController<BVGDriveConfig, GdriveSourceWebController.GdriveSourceFormData>(sourcesConfigProvider) {
    class GdriveSourceFormData(
            val sourceName: String,
            val key: String?,
            val secret: String?
    )

    override fun getRedirectAfterSaveView(config: BVGDriveConfig) =
            RedirectView(oauthController.getAuthTokenUrl(config))

    override fun getConfigClass() = BVGDriveConfig::class.java

    override fun mapConfig(sourceFormData: GdriveSourceFormData) =
            BVGDriveConfig (
                    sourceName = sourceFormData.sourceName,
                    clientId = sourceFormData.key!!,
                    clientSecret = sourceFormData.secret!!)

    override fun mapForm(config: BVGDriveConfig) =
            GdriveSourceFormData (
                    sourceName = config.sourceName,
                    key = config.clientId,
                    secret = config.clientSecret)
}
