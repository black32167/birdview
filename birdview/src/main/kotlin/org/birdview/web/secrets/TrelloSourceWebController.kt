package org.birdview.web.secrets

import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.model.secrets.BVTrelloSecret
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("${BVWebPaths.SECRETS}/trello")
class TrelloSourceWebController(
        sourceSecretsStorage: BVSourceSecretsStorage
): AbstractSourceWebController<BVTrelloSecret, TrelloSourceWebController.TrelloSourceFormData>(sourceSecretsStorage) {
    class TrelloSourceFormData(
            sourceName: String,
            user: String,
            val key: String?,
            val secret: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "trello")

    override fun getConfigClass() = BVTrelloSecret::class.java

    override fun mapConfig(sourceFormData: TrelloSourceFormData) =
            BVTrelloSecret(
                    sourceName = sourceFormData.sourceName,
                    key = sourceFormData.key!!,
                    token = sourceFormData.secret!!,
                    user = sourceFormData.user)

    override fun mapForm(config: BVTrelloSecret) = TrelloSourceFormData(
            sourceName = config.sourceName,
            key = config.key,
            secret = config.token,
            user = config.user
    )
}
