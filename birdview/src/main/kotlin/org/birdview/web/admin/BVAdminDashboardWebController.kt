package org.birdview.web.admin

import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserStorage
import org.birdview.web.BVTemplatePaths
import org.birdview.web.BVWebPaths
import org.birdview.web.secrets.BVSourceSecretListWebController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping(BVWebPaths.ADMIN_ROOT)
class BVAdminDashboardWebController(
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val userStorage: BVUserStorage
) {
    class UserWebView (
            val name: String,
            val enabled: Boolean
    )

    @GetMapping
    fun index(model: Model): String? {
        model
                .addAttribute("sources", sourceSecretsStorage.listSourceNames().map { mapSourceSetting(it) })
                .addAttribute("users", userStorage.listUserNames().map(this::mapUserView))
        return BVTemplatePaths.ADMIN_DASHBOARD
    }

    @PostMapping("user/update")
    fun updateUserState(userForm: UserWebView):String {
        userStorage.updateUserStatus(userForm.name, userForm.enabled)
        return "redirect:${BVWebPaths.ADMIN_ROOT}"
    }

    private fun mapUserView(userName: String): UserWebView =
            userStorage.getUserSettings(userName)
                    .let { userSetting ->
                        UserWebView(name = userName, enabled = userSetting.enabled) }

    private fun mapSourceSetting(sourceName: String): BVSourceSecretListWebController.SourceSettingView? =
            sourceSecretsStorage.getConfigByName(sourceName)
                    ?.let { sourceConfig ->
                        BVSourceSecretListWebController.SourceSettingView(
                                name = sourceConfig.sourceName,
                                type = sourceConfig.sourceType
                        )
                    }
}