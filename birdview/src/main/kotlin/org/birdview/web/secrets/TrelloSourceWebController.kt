package org.birdview.web.secrets

import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVTrelloConfig
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("${BVWebPaths.SECRETS}/trello")
class TrelloSourceWebController(
        sourceSecretsStorage: BVSourceSecretsStorage
): AbstractSourceWebController<BVTrelloConfig, TrelloSourceWebController.TrelloSourceFormData>(sourceSecretsStorage) {
    class TrelloSourceFormData(
            sourceName: String,
            user: String,
            val key: String?,
            val secret: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "trello")

    override fun getConfigClass() = BVTrelloConfig::class.java

    override fun mapConfig(sourceFormData: TrelloSourceFormData) =
            BVTrelloConfig(
                    sourceName = sourceFormData.sourceName,
                    key = sourceFormData.key!!,
                    token = sourceFormData.secret!!,
                    user = sourceFormData.user)

    override fun mapForm(config: BVTrelloConfig) = TrelloSourceFormData(
            sourceName = config.sourceName,
            key = config.key,
            secret = config.token,
            user = config.user
    )
}
