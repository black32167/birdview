package org.birdview.web.source

import org.birdview.config.BVGithubConfig
import org.birdview.config.BVSourcesConfigProvider
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/settings/github")
class GithubSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider
): AbstractSourceWebController<BVGithubConfig, GithubSourceWebController.GithubSourceFormData>(sourcesConfigProvider) {
    class GithubSourceFormData(
            val sourceName:String,
            val key: String?,
            val secret: String?
    )

    override fun getConfigClass() = BVGithubConfig::class.java

    override fun mapConfig(sourceFormData: GithubSourceFormData) =
            BVGithubConfig (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.key!!,
                    token = sourceFormData.secret!!)

    override fun mapForm(config: BVGithubConfig) =
            GithubSourceFormData(
                    sourceName = config.sourceName,
                    key = config.user,
                    secret = config.token)
}