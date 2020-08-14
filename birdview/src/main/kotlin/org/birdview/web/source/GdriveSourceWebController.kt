package org.birdview.web.source

import org.birdview.config.BVGDriveConfig
import org.birdview.config.BVSourcesConfigProvider
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/settings/gdrive")
class GdriveSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider
): AbstractSourceWebController<BVGDriveConfig, GdriveSourceWebController.GdriveSourceFormData>(sourcesConfigProvider) {
    class GdriveSourceFormData(
            val sourceName: String,
            val key: String?,
            val secret: String?
    )

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
