package org.birdview.web.source

import org.birdview.config.sources.BVJiraConfig
import org.birdview.config.sources.BVSourcesConfigStorage
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("${BVWebPaths.SECRETS}/jira")
class JiraSourceWebController(
        sourcesConfigStorage: BVSourcesConfigStorage
): AbstractSourceWebController<BVJiraConfig, JiraSourceWebController.JiraSourceFormData>(sourcesConfigStorage) {
    class JiraSourceFormData(
            sourceName:String,
            user: String,
            val secret: String?,
            val baseUrl: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "jira")

    override fun getConfigClass() = BVJiraConfig::class.java

    override fun mapConfig(sourceFormData: JiraSourceFormData) =
            BVJiraConfig (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.user,
                    token = sourceFormData.secret!!,
                    baseUrl = sourceFormData.baseUrl!!)

    override fun mapForm(config: BVJiraConfig) =
            JiraSourceFormData(
                sourceName = config.sourceName,
                baseUrl = config.baseUrl,
                secret = config.token,
                user = config.user)
}