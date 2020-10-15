package org.birdview.web.secrets

import org.birdview.storage.BVGithubConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("${BVWebPaths.SECRETS}/github")
class GithubSourceWebController(
        sourceSecretsStorage: BVSourceSecretsStorage
): AbstractSourceWebController<BVGithubConfig, GithubSourceWebController.GithubSourceFormData>(sourceSecretsStorage) {
    class GithubSourceFormData(
            sourceName:String,
            user: String,
            val secret: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "github")

    override fun getConfigClass() = BVGithubConfig::class.java

    override fun mapConfig(sourceFormData: GithubSourceFormData) =
            BVGithubConfig (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.user,
                    token = sourceFormData.secret!!)

    override fun mapForm(config: BVGithubConfig) =
            GithubSourceFormData(
                    sourceName = config.sourceName,
                    secret = config.token,
                    user = config.user)
}