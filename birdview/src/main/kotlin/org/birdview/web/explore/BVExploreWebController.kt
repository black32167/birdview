package org.birdview.web.explore

import org.birdview.model.*
import org.birdview.security.UserContext
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserStorage
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Controller
@RequestMapping(BVWebPaths.EXPLORE)
class BVExploreWebController(
        private val userStorage: BVUserStorage,
        private val sourceSecretsStorage: BVSourceSecretsStorage
) {
    class ReportLink(val reportUrl:String, val reportName:String)

    @GetMapping
    fun index(model: Model,
              @RequestParam(value = "user", required = false) user: String?,
              @RequestParam(value = "report", required = false) report: String?
    ): String? {
        val baseUrl = getBaseUrl()

        model.asMap().putAll(mapOf(
                "user" to (user ?: UserContext.getUserName()),
                "baseURL" to baseUrl,
                "reportTypes" to ReportType.values(),
                "representationTypes" to RepresentationType.values(),
                "userRoles" to UserRole.values(),
                "sources" to sourceSecretsStorage.listSourceNames(),
                "users" to listUsers()
        ))
        return "/report"
    }

    private fun listUsers() =
            userStorage.listUserNames()

    private fun getBaseUrl() =
            ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()

}