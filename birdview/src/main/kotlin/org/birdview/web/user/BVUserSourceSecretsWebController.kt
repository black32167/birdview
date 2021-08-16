package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(BVWebPaths.USER_SETTINGS)
class BVUserSourceSecretsWebController(
    private val userSourceStorage: BVUserSourceConfigStorage
) {
    @GetMapping("source/{sourceName}/add-user-source-secret")
    fun editForm(model: Model, @PathVariable("sourceName") sourceName:String): String {
        userSourceStorage.getSource(currentUserName(), sourceName)???
     //   Add source type into BVUserSourceConfig, select creds the same way using dropdown as for 'default' sources
//        val sourceProfile = userSourceStorage.getSourceProfile(bvUser = currentUserName(), sourceName = sourceName)
        model
            .addAttribute("sourceName", sourceName)
//            .addAttribute("sourceUserName", sourceProfile.sourceUserName)
//            .addAttribute("enabled", if (sourceProfile.enabled) BVUserSourcesListWebController.UpdateUserSourceFormData.YES else BVUserSourcesListWebController.UpdateUserSourceFormData.NO)
        return "/user/add-user-source-secret"
    }

    private fun currentUserName() = UserContext.getUserName()
}
