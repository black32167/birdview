package org.birdview.web.admin

import org.birdview.storage.BVUserStorage
import org.birdview.web.BVTemplatePaths
import org.birdview.web.BVWebPaths
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@Controller
@RequestMapping(BVWebPaths.ADMIN_ROOT)
class BVAdminDashboardWebController(
        private val userStorage: BVUserStorage
) {
    private val log = LoggerFactory.getLogger(BVAdminDashboardWebController::class.java)

    class UserWebView (
            val name: String,
            val enabled: Boolean
    )

    @GetMapping
    fun index(model: Model): String? {
        model
                .addAttribute("users", userStorage.listUserNames().map(this::mapUserView))
        return BVTemplatePaths.ADMIN_DASHBOARD
    }

    @PostMapping("user/update")
    fun updateUserState(userForm: UserWebView):String {
        userStorage.updateUserStatus(userForm.name, userForm.enabled)
        return "redirect:${BVWebPaths.ADMIN_ROOT}"
    }

    @GetMapping("user/delete")
    fun deleteUser(@RequestParam("user") user:String):String {
        try {
            userStorage.delete(user)
        } catch (e: Exception) {
            log.error("", e);
        }
        return "redirect:${BVWebPaths.ADMIN_ROOT}"
    }

    private fun mapUserView(userName: String): UserWebView =
            userStorage.getUserSettings(userName)
                    .let { userSetting ->
                        UserWebView(name = userName, enabled = userSetting.enabled) }

}