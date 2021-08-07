package org.birdview.web.secrets

import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.model.secrets.BVConfluenceSecret
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("${BVWebPaths.SECRETS}/confluence")
class ConfluenceWebController(
        sourceSecretsStorage: BVSourceSecretsStorage
): AbstractSourceWebController<BVConfluenceSecret, ConfluenceWebController.ConfluenceSourceFormData>(sourceSecretsStorage) {
    class ConfluenceSourceFormData(
            sourceName:String,
            user: String,
            val secret: String?,
            val baseUrl: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "confluence")

    override fun mapConfig(sourceFormData: ConfluenceSourceFormData): BVConfluenceSecret =
            BVConfluenceSecret (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.user,
                    token = sourceFormData.secret!!,
                    baseUrl = sourceFormData.baseUrl!!)

    override fun mapForm(config: BVConfluenceSecret): ConfluenceSourceFormData =
            ConfluenceSourceFormData (
                    sourceName = config.sourceName,
                    baseUrl = config.baseUrl,
                    secret = config.token,
                    user = config.user)

    override fun getConfigClass(): Class<BVConfluenceSecret> = BVConfluenceSecret::class.java
}