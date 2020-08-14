package org.birdview.web.source

import org.birdview.config.BVJiraConfig
import org.birdview.config.BVSourcesConfigProvider
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/settings/jira")
class JiraSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider
): AbstractSourceWebController<BVJiraConfig, JiraSourceWebController.JiraSourceFormData>(sourcesConfigProvider) {
    class JiraSourceFormData(
            val sourceName:String,
            val key: String?,
            val secret: String?,
            val baseUrl: String?
    )

    override fun getConfigClass() = BVJiraConfig::class.java

    override fun mapConfig(sourceFormData: JiraSourceFormData) =
            BVJiraConfig (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.key!!,
                    token = sourceFormData.secret!!,
                    baseUrl = sourceFormData.baseUrl!!)

    override fun mapForm(config: BVJiraConfig) =
            JiraSourceFormData(
                sourceName = config.sourceName,
                key = config.user,
                baseUrl = config.baseUrl,
                secret = config.token)
}