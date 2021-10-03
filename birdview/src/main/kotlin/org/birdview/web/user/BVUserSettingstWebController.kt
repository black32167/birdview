package org.birdview.web.user

import org.birdview.security.PasswordUtils
import org.birdview.security.UserContext
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.BVUserStorage
import org.birdview.web.BVWebPaths
import org.birdview.web.BVWebTimeZonesUtil
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping(BVWebPaths.USER_SETTINGS)
class BVUserSettingstWebController (
    private val userSourceStorage: BVUserSourceConfigStorage,
    private val userStorage: BVUserStorage,

) {
    companion object {
        fun getPath() = BVWebPaths.USER_SETTINGS
        val USER_SETTINGS_TEMPLATE = "/user/user-settings"
        val MESSAGE = "message"
    }

    class UpdateZoneFormData(
        val zoneId: String
    )

    class UpdatePasswordFormData(
        val newPassword: String
    )

    class AddWorkgroupFormData(
        val workGroup: String
    )

    @GetMapping
    fun index(model: Model, @RequestParam(value = "message", required = false) message:String?): String {
        model.addAttribute(MESSAGE, message);
        setPageContext(model)
        return USER_SETTINGS_TEMPLATE
    }

    @PostMapping("profile")
    // TODO: non transactional
    fun updateProfile(redirectAttributes: RedirectAttributes, updateZoneFormData: UpdateZoneFormData): String {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        userStorage.update(loggedUser, settings.copy(zoneId = updateZoneFormData.zoneId))

        redirectAttributes.addAttribute(MESSAGE, "Time zone updated!")

        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    @PostMapping("password")
    // TODO: non transactional
    fun updatePassword(redirectAttributes: RedirectAttributes, updatePasswordFormData: UpdatePasswordFormData): String {
        val newPassword = updatePasswordFormData.newPassword
        if (newPassword.isEmpty()) {
            redirectAttributes.addAttribute(MESSAGE, "Password WAS NOT changed!")
        } else {
            val loggedUser = UserContext.getUserName()
            val settings = userStorage.getUserSettings(loggedUser)

            userStorage.update(loggedUser, settings.copy(passwordHash = PasswordUtils.hash(newPassword)))

            redirectAttributes.addAttribute(MESSAGE, "Password changed!")
        }
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    private fun currentUserName() = UserContext.getUserName()

    private fun setPageContext(model: Model) {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        model
            .addAttribute("sourceNames", userSourceStorage.listSourceNames(currentUserName()))
            .addAttribute("availableTimeZoneIds", BVWebTimeZonesUtil.getAvailableTimezoneIds())
            .addAttribute("user", settings)
    }
}
