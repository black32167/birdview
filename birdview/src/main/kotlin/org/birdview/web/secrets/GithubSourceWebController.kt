package org.birdview.web.secrets

import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.model.secrets.BVGithubSecret
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("${BVWebPaths.SECRETS}/github")
class GithubSourceWebController(
        sourceSecretsStorage: BVSourceSecretsStorage
): AbstractSourceWebController<BVGithubSecret, GithubSourceWebController.GithubSourceFormData>(sourceSecretsStorage) {
    class GithubSourceFormData(
            sourceName:String,
            user: String,
            val secret: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "github")

    override fun getConfigClass() = BVGithubSecret::class.java

    override fun mapConfig(sourceFormData: GithubSourceFormData) =
            BVGithubSecret (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.user,
                    token = sourceFormData.secret!!)

    override fun mapForm(config: BVGithubSecret) =
            GithubSourceFormData(
                    sourceName = config.sourceName,
                    secret = config.token,
                    user = config.user)
}