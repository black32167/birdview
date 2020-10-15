package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserSourceStorage
import org.birdview.storage.model.BVUserSourceConfig
import org.birdview.web.BVWebPaths
import org.birdview.web.user.BVUserSourcesListWebController.UpdateUserSourceFormData.Companion.NO
import org.birdview.web.user.BVUserSourcesListWebController.UpdateUserSourceFormData.Companion.YES
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(BVWebPaths.USER_SOURCES)
class BVUserSourcesListWebController (
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val userSourceStorage: BVUserSourceStorage,
        private val userStorage: BVUserSourceStorage
) {
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
        model
                .addAttribute("sourceNames",
                        userStorage.listUserSources(currentUserName()))
        return "/user/list-sources"
    }

    @GetMapping("{sourceName}/edit")
    fun editForm(model: Model, @PathVariable("sourceName") sourceName:String): String {
        val sourceProfile = userSourceStorage.getSourceProfile(bvUserName = currentUserName(), sourceName = sourceName)
        model
                .addAttribute("sourceUserName", sourceProfile.sourceUserName)
                .addAttribute("enabled", if (sourceProfile.enabled) YES else NO)
        return "/user/edit-source"
    }

    @GetMapping("{sourceName}/delete")
    fun delete(model: Model, @PathVariable("sourceName") sourceName:String): String {
        userSourceStorage.delete(bvUserName = currentUserName(), sourceName = sourceName)
        return "redirect:${BVWebPaths.USER_SOURCES}"
    }

    @GetMapping("add")
    fun addForm(model: Model): String {
        model
                .addAttribute("availableSourceNames",
                        sourceSecretsStorage.listSourceNames())
        return "/user/add-source"
    }

    @PostMapping("{sourceName}")
    fun update(@PathVariable("sourceName") sourceName:String,
               formDataUpdate:UpdateUserSourceFormData): String {
        userSourceStorage.update(
                bvUserName = currentUserName(),
                sourceName = sourceName,
                userProfileSourceConfig = BVUserSourceConfig(formDataUpdate.sourceUserName, formDataUpdate.enabled != null))
        return "redirect:${BVWebPaths.USER_SOURCES}"
    }

    @PostMapping
    fun add(formDataCreate:CreateUserSourceFormData): String {
        userSourceStorage.create(
                bvUserName = currentUserName(),
                sourceUserName = formDataCreate.sourceUserName,
                sourceName = formDataCreate.sourceName)
        return "redirect:${BVWebPaths.USER_SOURCES}"
    }

    private fun currentUserName() = UserContext.getUserName()
}