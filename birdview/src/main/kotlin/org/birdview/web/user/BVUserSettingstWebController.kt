package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.BVUserStorage
import org.birdview.web.BVWebPaths
import org.birdview.web.BVWebTimeZonesUtil
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(BVWebPaths.USER_SETTINGS)
class BVUserSettingstWebController (
    private val userSourceStorage: BVUserSourceConfigStorage,
    private val userStorage: BVUserStorage,

) {
    companion object {
        fun getPath() = BVWebPaths.USER_SETTINGS
    }

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


    private fun currentUserName() = UserContext.getUserName()

}
