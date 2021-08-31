package org.birdview.web.explore

import org.birdview.model.ReportType
import org.birdview.model.RepresentationType
import org.birdview.model.UserRole
import org.birdview.security.UserContext
import org.birdview.source.BVSourceConfigProvider
import org.birdview.storage.BVUserStorage
import org.birdview.web.BVWebPaths
import org.birdview.web.WebUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping(BVWebPaths.EXPLORE)
class BVExploreWebController(
    private val userStorage: BVUserStorage,
    private val sourceConfigProvider: BVSourceConfigProvider
) {
    @GetMapping
    fun index(model: Model,
              @RequestParam(value = "user", required = false) user: String?,
              @RequestParam(value = "report", required = false) report: String?
    ): String? {
        val baseUrl = WebUtils.getBaseUrl()

        val inferredUser = (user ?: UserContext.getUserName())
        model.asMap().putAll(mapOf(
                "user" to inferredUser,
                "baseURL" to baseUrl,
                "reportTypes" to ReportType.values(),
                "representationTypes" to RepresentationType.values(),
                "userRoles" to UserRole.values(),
                "sources" to sourceConfigProvider.listEnabledSourceConfigs(inferredUser).map { it.sourceName },
                "users" to listUsers()
        ))
        return "/report"
    }

    private fun listUsers(): List<String> {
        val currentUser = UserContext.getUserName()
        val usersInWorkgroup = userStorage.getUserSettings(userName = currentUser)
            .let { userSettings -> userStorage.getUsersInWorkGroup(userSettings.workGroups) }
        return usersInWorkgroup.takeIf { it.isNotEmpty() }
            ?: listOf(currentUser)
    }
}
