package org.birdview.web.source

import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVTrelloConfig
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/settings/trello")
class TrelloSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider
): AbstractSourceWebController<BVTrelloConfig, TrelloSourceWebController.TrelloSourceFormData>(sourcesConfigProvider) {
    class TrelloSourceFormData(
            val sourceName: String,
            val key: String?,
            val secret: String?
    )

    override fun getConfigClass() = BVTrelloConfig::class.java

    override fun mapConfig(sourceFormData: TrelloSourceFormData) =
            BVTrelloConfig(
                    sourceName = sourceFormData.sourceName,
                    key = sourceFormData.key!!,
                    token = sourceFormData.secret!!)

    override fun mapForm(config: BVTrelloConfig) = TrelloSourceFormData(
            sourceName = config.sourceName,
            key = config.key,
            secret = config.token)
}
