package org.birdview.web.secrets

import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.model.secrets.BVJiraSecret
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("${BVWebPaths.SECRETS}/jira")
class JiraSourceWebController(
        sourceSecretsStorage: BVSourceSecretsStorage
): AbstractSourceWebController<BVJiraSecret, JiraSourceWebController.JiraSourceFormData>(sourceSecretsStorage) {
    class JiraSourceFormData(
            sourceName:String,
            user: String,
            val secret: String?,
            val baseUrl: String?
    ): AbstractSourceFormData (sourceName = sourceName, user = user, type = "jira")

    override fun getConfigClass() = BVJiraSecret::class.java

    override fun mapConfig(sourceFormData: JiraSourceFormData) =
            BVJiraSecret (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.user,
                    token = sourceFormData.secret!!,
                    baseUrl = sourceFormData.baseUrl!!)

    override fun mapForm(config: BVJiraSecret) =
            JiraSourceFormData(
                sourceName = config.sourceName,
                baseUrl = config.baseUrl,
                secret = config.token,
                user = config.user)
}