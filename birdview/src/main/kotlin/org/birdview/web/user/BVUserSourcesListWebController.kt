package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVSourcesProvider
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.BVUserStorage
import org.birdview.storage.SourceSecretsMapper
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.web.BVWebPaths
import org.birdview.web.BVWebTimeZonesUtil
import org.birdview.web.form.CreateUserSourceFormData
import org.birdview.web.form.UpdateUserSourceFormData
import org.birdview.web.form.UpdateUserSourceFormData.Companion.NO
import org.birdview.web.form.UpdateUserSourceFormData.Companion.YES
import org.birdview.web.form.secret.SourceSecretFormData
import org.birdview.web.secrets.OAuthSourceWebController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.view.RedirectView
import javax.ws.rs.NotFoundException

@Controller
@RequestMapping(BVWebPaths.USER_SETTINGS)
class BVUserSourcesListWebController (
    private val userSourceStorage: BVUserSourceConfigStorage,
    private val userStorage: BVUserStorage,
    private val sourcesProvider: BVSourcesProvider,
    private val sourceSecretsMapper: SourceSecretsMapper
) {
    class ProfileFormData(
        val zoneId: String
    )

    @GetMapping
    fun index(model: Model): String {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        model
            .addAttribute("sourceNames", userSourceStorage.listSourceNames(currentUserName()))
            .addAttribute("availableTimeZoneIds", BVWebTimeZonesUtil.getAvailableTimezoneIds())
            .addAttribute("profileForm", settings)

        return "/user/user-settings"
    }

    @PostMapping
    fun updateProfile(profileFormData: ProfileFormData): String {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        userStorage.update(loggedUser, settings.copy(zoneId = profileFormData.zoneId))
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @GetMapping("source/{sourceName}/edit")
    fun editForm(model: Model, @PathVariable("sourceName") sourceName:String): String {
        val sourceConfig = userSourceStorage.getSource(bvUser = currentUserName(), sourceName = sourceName)
            ?: throw NotFoundException("Unknown source: '${sourceName}'")
        model
                .addAttribute("sourceUserName", sourceConfig.sourceUserName)
                .addAttribute("enabled", if (sourceConfig.enabled) YES else NO)
        return "/user/edit-source"
    }

    @GetMapping("source/{sourceName}/delete")
    fun delete(model: Model, @PathVariable("sourceName") sourceName:String): String {
        userSourceStorage.delete(bvUser = currentUserName(), sourceName = sourceName)
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @GetMapping("source/add")
    fun addForm(model: Model): String {
        model
            .addAttribute(
                "availableSourceNames",
                sourcesProvider.listAvailableSourceNames()
            )
        return "/user/add-source"
    }

    @PostMapping("source/{sourceName}")
    fun update(@PathVariable("sourceName") sourceName:String,
               formDataUpdate: UpdateUserSourceFormData
    ): Any {
        val bvUser = currentUserName()
        val sourceManager = sourcesProvider.getBySourceName(
            bvUser = bvUser, sourceName = sourceName)

        if(sourceManager != null) {
            val resolved = sourceManager.resolveSourceUserId(bvUser, sourceName, formDataUpdate.sourceUserName)
            println(resolved)
        }

        val persistentSecret = toPersistent(formDataUpdate.sourceSecretFormData)
        userSourceStorage.update(
            bvUser = bvUser,
            sourceConfig = BVUserSourceConfig(
                sourceName = sourceName,
                sourceUserName = formDataUpdate.sourceUserName,
                enabled = formDataUpdate.enabled != null,
                baseUrl = formDataUpdate.baseUrl,
                sourceType = formDataUpdate.sourceType,
                serializedSourceSecret = sourceSecretsMapper.serialize(toPersistent(formDataUpdate.sourceSecretFormData))
            )
        )
        return getRedirectAfterSaveView(sourceName, persistentSecret)
    }

    private fun toPersistent(sourceSecretFormData: SourceSecretFormData): BVSourceSecret {
        TODO("Not yet implemented")
    }

    @PostMapping("source")
    fun add(formDataCreate: CreateUserSourceFormData): Any {
        val persistentSecret = toPersistent(formDataCreate.sourceSecretFormData)
        userSourceStorage.create(
            bvUser = currentUserName(),
            sourceConfig = BVUserSourceConfig(
                sourceName = formDataCreate.sourceName,
                sourceUserName = formDataCreate.sourceUserName,
                enabled = false,
                baseUrl = formDataCreate.baseUrl,
                sourceType = formDataCreate.sourceType,
                serializedSourceSecret = sourceSecretsMapper.serialize(persistentSecret)
            )
        )
        return getRedirectAfterSaveView(formDataCreate.sourceName, persistentSecret)
    }

    private fun currentUserName() = UserContext.getUserName()

    private fun getRedirectAfterSaveView(sourceName: String, secret: BVSourceSecret): Any =
        if (secret is BVOAuthSourceSecret) {
            RedirectView(getProviderAuthCodeUrl(sourceName = sourceName, oAuthConfig = secret))
        } else {
            "redirect:${BVWebPaths.USER_SETTINGS}"
        }

    private fun getProviderAuthCodeUrl(sourceName: String, oAuthConfig: BVOAuthSourceSecret): String {
        val req = oAuthConfig.authCodeUrl +
                "client_id=${oAuthConfig.clientId}" +
                "&response_type=code" +
                "&redirect_uri=${OAuthSourceWebController.getRedirectCodeUrl(sourceName)}" +
                "&scope=${oAuthConfig.scope}" +
                "&access_type=offline"
        return req
    }
}
