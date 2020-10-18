package org.birdview.web.admin

import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserStorage
import org.birdview.web.BVTemplatePaths
import org.birdview.web.BVWebPaths
import org.birdview.web.secrets.BVSourceSecretListWebController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping(BVWebPaths.ADMIN_ROOT)
class BVAdminWebController(
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val userStorage: BVUserStorage
) {
    @GetMapping
    fun index(model: Model): String? {
        model
                .addAttribute("sources", sourceSecretsStorage.listSourceNames().map { mapSourceSetting(it) })
                .addAttribute("userNames", userStorage.listUsers())
        return BVTemplatePaths.ADMIN_DASHBOARD
    }

    private fun mapSourceSetting(sourceName: String): BVSourceSecretListWebController.SourceSettingView? =
            sourceSecretsStorage.getConfigByName(sourceName)
                    ?.let { sourceConfig ->
                        BVSourceSecretListWebController.SourceSettingView(
                                name = sourceConfig.sourceName,
                                type = sourceConfig.sourceType
                        )
                    }
}