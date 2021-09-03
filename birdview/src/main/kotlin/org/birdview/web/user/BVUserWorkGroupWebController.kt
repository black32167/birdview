package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVUserStorage
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/user/group")
class BVUserWorkGroupWebController(
    private val userStorage: BVUserStorage,
) {

    @PostMapping
    fun addWorkgroup(addWorkgroupFormData: BVUserSettingstWebController.AddWorkgroupFormData): String {
        val loggedUser = UserContext.getUserName()
        val settings = userStorage.getUserSettings(loggedUser)

        userStorage.update(loggedUser, settings.copy(workGroups = settings.workGroups + addWorkgroupFormData.workGroup))
        return "redirect:${BVUserSettingstWebController.getPath()}"
    }

    @GetMapping("{groupName}/delete")
    fun deleteGroup(model: Model, @PathVariable("groupName") groupName:String): String {
        userStorage.deleteGroup(bvUser = UserContext.getUserName(), groupName = groupName)
        return "redirect:${BVUserSettingstWebController.getPath()}"
    }
}
