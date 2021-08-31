package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVSourcesProvider
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.BVUserStorage
import org.birdview.storage.SourceSecretsMapper
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.birdview.storage.model.source.secrets.BVLentSecrets
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.storage.model.source.secrets.BVTokenSourceSecret
import org.birdview.web.BVWebPaths
import org.birdview.web.BVWebTimeZonesUtil
import org.birdview.web.form.CreateUserSourceFormData
import org.birdview.web.form.UpdateUserSourceFormData
import org.birdview.web.form.UpdateUserSourceFormData.Companion.NO
import org.birdview.web.form.UpdateUserSourceFormData.Companion.YES
import org.birdview.web.form.secret.*
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
class BVUserSettingstWebController (
    private val userSourceStorage: BVUserSourceConfigStorage,
    private val userStorage: BVUserStorage,
    private val sourcesProvider: BVSourcesProvider,
    private val sourceSecretsMapper: SourceSecretsMapper
) {
    class UpdateUserFormData(
        val zoneId: String
    )

    class AddWorkgroupFormData(
        val workGroup: String
    )

    @GetMapping
    fun index(model: Model): String {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        model
            .addAttribute("sourceNames", userSourceStorage.listSourceNames(currentUserName()))
            .addAttribute("availableTimeZoneIds", BVWebTimeZonesUtil.getAvailableTimezoneIds())
            .addAttribute("user", settings)

        return "/user/user-settings"
    }

    @PostMapping("profile")
    fun updateProfile(updateUserFormData: UpdateUserFormData): String {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        userStorage.update(loggedUser, settings.copy(zoneId = updateUserFormData.zoneId))
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @PostMapping("addWorkGroup")
    fun addWorkgroup(addWorkgroupFormData: AddWorkgroupFormData): String {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        userStorage.update(loggedUser, settings.copy(workGroups = settings.workGroups + addWorkgroupFormData.workGroup))
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @GetMapping("source/{sourceName}/edit")
    fun editForm(model: Model, @PathVariable("sourceName") sourceName:String): String {
        val sourceConfig = userSourceStorage.getSource(bvUser = currentUserName(), sourceName = sourceName)
            ?: throw NotFoundException("Unknown source: '${sourceName}'")
        val secret = sourceSecretsMapper.deserialize(sourceConfig.serializedSourceSecret)
        model
            .addAttribute("sourceName", sourceName)
            .addAttribute("baseUrl", sourceConfig.baseUrl)
            .addAttribute("sourceType", sourceConfig.sourceType.alias)
            .addAttribute("sourceUserName", sourceConfig.sourceUserName)
            .addAttribute("enabled", if (sourceConfig.enabled) YES else NO)
            .addAttribute("secret", secret)
        if(secret is BVOAuthSourceSecret) {
            model.addAttribute("authorizationUrl", getProviderAuthCodeUrl(sourceName, secret))
        }
        return "/user/edit-source"
    }

    @GetMapping("source/{sourceName}/delete")
    fun deleteSource(model: Model, @PathVariable("sourceName") sourceName:String): String {
        userSourceStorage.delete(bvUser = currentUserName(), sourceName = sourceName)
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @GetMapping("group/{groupName}/delete")
    fun deleteGroup(model: Model, @PathVariable("groupName") groupName:String): String {
        userStorage.deleteGroup(bvUser = currentUserName(), groupName = groupName)
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @GetMapping("source/add")
    fun addForm(model: Model): String {
        model
            .addAttribute("sourceTypes", sourcesProvider.listAvailableSourceTypes().map { it.name.toLowerCase() })
        return "/user/add-source"
    }

    @PostMapping("source/{sourceName}")
    fun update(@PathVariable("sourceName") sourceName:String,
               formDataUpdate: UpdateUserSourceFormData
    ): Any {
        val bvUser = currentUserName()
//        val sourceManager = sourcesProvider.getBySourceName(
//            bvUser = bvUser, sourceName = sourceName)
//
//        if(sourceManager != null) {
//            val resolved = sourceManager.resolveSourceUserId(bvUser, sourceName, formDataUpdate.sourceUserName)
//            println(resolved)
//        }

        userSourceStorage.update(
            bvUser = bvUser,
            sourceConfig = BVUserSourceConfig(
                sourceName = sourceName,
                sourceUserName = resolveSourceUseName(formDataUpdate.sourceSecretFormData),
                enabled = formDataUpdate.enabled != null,
                baseUrl = formDataUpdate.baseUrl,
                sourceType = formDataUpdate.sourceType,
                serializedSourceSecret = sourceSecretsMapper.serialize(toPersistent(formDataUpdate.sourceSecretFormData))
            )
        )
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @PostMapping("source")
    fun add(formDataCreate: CreateUserSourceFormData): Any {
        val persistentSecret = toPersistent(formDataCreate.sourceSecretFormData)
        userSourceStorage.create(
            bvUser = currentUserName(),
            sourceConfig = BVUserSourceConfig(
                sourceName = formDataCreate.sourceName,
                sourceUserName = resolveSourceUseName(formDataCreate.sourceSecretFormData),
                enabled = false,
                baseUrl = formDataCreate.baseUrl,
                sourceType = formDataCreate.sourceType,
                serializedSourceSecret = sourceSecretsMapper.serialize(persistentSecret)
            )
        )
        return getRedirectAfterSaveView(formDataCreate.sourceName, persistentSecret)
    }

    private fun toPersistent(formData: SourceSecretFormData): BVSourceSecret {
        val secret = formData.getSecretToken()
        val LEND_PREFIX = "lend_from:"
        if (secret.startsWith(LEND_PREFIX)) {
            val (lender, sourceName) = secret.substring(LEND_PREFIX.length).split(":".toRegex(), 2)
            return BVLentSecrets(lender, sourceName)
        }

        return when (formData) {
            is JiraSourceSecretFormData -> BVTokenSourceSecret(
                user = formData.user,
                token = formData.secret
            )
            is ConfluenceSourceSecretFormData -> BVTokenSourceSecret(
                user = formData.email,
                token = formData.secret
            )
            is GithubSourceSecretFormData -> BVTokenSourceSecret(
                user = formData.user,
                token = formData.secret
            )
            is TrelloSourceSecretFormData -> BVTokenSourceSecret(
                user = formData.key,
                token = formData.secret
            )
            is GdriveSourceSecretFormData -> BVOAuthSourceSecret(
                flavor = BVOAuthSourceSecret.OAuthFlavour.GDRIVE,
                clientId = formData.clientId,
                clientSecret = formData.clientSecret,
                authCodeUrl = "https://accounts.google.com/o/oauth2/v2/auth?",
                tokenExchangeUrl = "https://oauth2.googleapis.com/token",
                scope = "https://www.googleapis.com/auth/drive"
            )
            is SlackSourceSecretFormData -> BVOAuthSourceSecret(
                flavor = BVOAuthSourceSecret.OAuthFlavour.SLACK,
                clientId = formData.clientId,
                clientSecret = formData.clientSecret,
                authCodeUrl = "https://slack.com/oauth/v2/authorize?user_scope=identity.basic&",
                tokenExchangeUrl = "https://slack.com/api/oauth.v2.access",
                scope = "channels:history,channels:read"
            )
            else -> throw UnsupportedOperationException("Unsupported secret form class: ${formData.javaClass}")
        }
    }

    private fun resolveSourceUseName(formData: SourceSecretFormData) =
        when (formData) {
            is JiraSourceSecretFormData -> formData.user
            is ConfluenceSourceSecretFormData -> formData.user
            is GithubSourceSecretFormData -> formData.user
            is TrelloSourceSecretFormData -> formData.email
            is GdriveSourceSecretFormData -> formData.email
            is SlackSourceSecretFormData -> formData.email
            else -> throw UnsupportedOperationException("Unsupported secret form class: ${formData.javaClass}")
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
