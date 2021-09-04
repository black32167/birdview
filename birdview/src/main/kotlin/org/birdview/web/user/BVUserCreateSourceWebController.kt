package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVSourcesProvider
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.SourceSecretsMapper
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.web.form.CreateUserSourceFormData
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("/user/source")
class BVUserCreateSourceWebController(
    private val sourcesProvider: BVSourcesProvider,
    private val userSourceStorage: BVUserSourceConfigStorage,
    private val sourceSecretsMapper: SourceSecretsMapper
): BVUserSourceWebControllerSupport() {

    @GetMapping
    fun addForm(model: Model): String {
        model
            .addAttribute("sourceTypes", sourcesProvider.listAvailableSourceTypes().map { it.name.toLowerCase() })
        return "/user/add-source"
    }

    @PostMapping
    fun add(formDataCreate: CreateUserSourceFormData): Any {
        val persistentSecret = toPersistent(formDataCreate.sourceName, formDataCreate.sourceType, formDataCreate.sourceSecretFormData, formDataCreate.filter)
        userSourceStorage.create(
            bvUser = UserContext.getUserName(),
            sourceConfig = BVUserSourceConfig(
                sourceName = formDataCreate.sourceName,
                sourceUserName = formDataCreate.filter,
                enabled = false,
                baseUrl = formDataCreate.baseUrl,
                sourceType = formDataCreate.sourceType,
                serializedSourceSecret = sourceSecretsMapper.serialize(persistentSecret)
            )
        )
        return getRedirectAfterSaveView(formDataCreate.sourceName, persistentSecret)
    }

    private fun getRedirectAfterSaveView(sourceName: String, secret: BVSourceSecret): Any =
        if (secret is BVOAuthSourceSecret) {
            RedirectView(getProviderAuthCodeUrl(sourceName = sourceName, oAuthConfig = secret))
        } else {
            "redirect:${BVUserSettingstWebController.getPath()}"
        }

}