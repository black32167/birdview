package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVDocumentProvidersManager
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSourceConfig
import org.birdview.web.BVWebPaths
import org.birdview.web.BVWebTimeZonesUtil
import org.birdview.web.user.BVUserSourcesListWebController.UpdateUserSourceFormData.Companion.NO
import org.birdview.web.user.BVUserSourcesListWebController.UpdateUserSourceFormData.Companion.YES
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(BVWebPaths.USER_SETTINGS)
class BVUserSourcesListWebController (
    private val sourceSecretsStorage: BVSourceSecretsStorage,
    private val userSourceStorage: BVUserSourceStorage,
    private val userStorage: BVUserStorage,
    private val sourcesManager: BVDocumentProvidersManager
) {
    class ProfileFormData(
        val zoneId: String
    )

    class CreateUserSourceFormData (
            val sourceName: String,
            val sourceUserName: String
    )
    class UpdateUserSourceFormData (
            val sourceUserName: String,
            val enabled: String?
    ) {
        companion object {
            val YES = "yes"
            val NO = "no"
        }
    }

    @GetMapping
    fun index(model: Model): String {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        model
            .addAttribute("sourceNames", userSourceStorage.listUserSources(currentUserName()))
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
        val sourceProfile = userSourceStorage.getSourceProfile(bvUser = currentUserName(), sourceName = sourceName)
        model
                .addAttribute("sourceUserName", sourceProfile.sourceUserName)
                .addAttribute("enabled", if (sourceProfile.enabled) YES else NO)
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
                .addAttribute("availableSourceNames",
                        sourceSecretsStorage.listSourceNames())
        return "/user/add-source"
    }

    @PostMapping("source/{sourceName}")
    fun update(@PathVariable("sourceName") sourceName:String,
               formDataUpdate:UpdateUserSourceFormData): String {
        val sourceManager = sourcesManager.getBySourceName(sourceName)

        if(sourceManager != null) {
            val resolved = sourceManager.resolveSourceUserId(sourceName, formDataUpdate.sourceUserName)
            println(resolved)
        }

        userSourceStorage.update(
            bvUser = currentUserName(),
            userProfileSourceConfig = BVUserSourceConfig(
                sourceName = sourceName,  sourceUserName = formDataUpdate.sourceUserName, enabled = formDataUpdate.enabled != null
            )
        )
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @PostMapping("source")
    fun add(formDataCreate:CreateUserSourceFormData): String {
        userSourceStorage.create(
            bvUser = currentUserName(),
            sourceName = formDataCreate.sourceName,
            sourceUserName = formDataCreate.sourceUserName
        )
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    private fun currentUserName() = UserContext.getUserName()
}