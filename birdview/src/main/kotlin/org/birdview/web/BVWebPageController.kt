package org.birdview.web

import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Controller
class BVWebPageController(
        private val usersConfigProvider: BVUsersConfigProvider,
        private val sourcesConfigProvider: BVSourcesConfigProvider
) {
    class ReportLink(val reportUrl:String, val reportName:String)
    class OAuthCodeLink(val source: String, val authCodeUrl:String)

    @GetMapping("/")
    fun index(model: Model,
              @RequestParam(value = "user", required = false) user: String?,
              @RequestParam(value = "report", required = false) report: String?
    ): String? {
        val baseUrl = getBaseUrl()
        val tsRequest = buildTSRequest(user, report)

        model.asMap().putAll(mapOf(
                "reportLinks" to ReportType.values()
                        .map {
                            ReportLink(
                                    reportUrl = reportUrl(it, tsRequest, baseUrl),
                                    reportName = it.name.toLowerCase().capitalize())
                        },
                "user" to tsRequest.userFilter,
                "baseURL" to baseUrl,
                "reportPath" to "report-${tsRequest.reportType}.ftl",
                "format" to getFormat(tsRequest.reportType),
                "reportTypes" to ReportType.values(),
                "representationTypes" to RepresentationType.values(),
                "userRoles" to UserRole.values(),
                "sources" to sourcesConfigProvider.listSourceNames(),
                "users" to listUsers()
        ))
        return "report"
    }

    private fun listUsers() =
            usersConfigProvider.listUsers()

    private fun reportUrl(reportType: ReportType, tsRequest: BVDocumentFilter, baseUrl: String): String {
        return "${baseUrl}?report=${reportType.name.toLowerCase()}" +
                (tsRequest.userFilter.takeIf { it.role == UserRole.IMPLEMENTOR }
                        ?.userAlias
                        ?.let { "&user=${it}" } ?: "")
    }

    private fun buildTSRequest(user: String?, report: String?) : BVDocumentFilter {
        val sourceType = null
        val reportType = report
                ?.toUpperCase()
                ?.let { ReportType.valueOf(it) }
                ?: ReportType.WORKED
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        return when(reportType) {
            ReportType.PLANNED -> BVDocumentFilter(
                    reportType = reportType,
                    grouping = true,
                    updatedPeriod = TimeIntervalFilter(),
                    userFilter = UserFilter( userAlias = user, role = UserRole.IMPLEMENTOR),
                    sourceType = sourceType)
            ReportType.WORKED -> BVDocumentFilter(
                    reportType = reportType,
                    grouping = true,
                    updatedPeriod = TimeIntervalFilter(after = today.minusDays(10)),
                    userFilter = UserFilter( userAlias = user, role = UserRole.IMPLEMENTOR),
                    sourceType = sourceType)
        }
    }

    private fun getFormat(reportType: ReportType): String = "long"/*when(reportType) {
        ReportType.LAST_DAY -> "brief"
        else -> "long"
    }*/

    private fun getBaseUrl() =
            ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()

}