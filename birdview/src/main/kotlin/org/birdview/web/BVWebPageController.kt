package org.birdview.web

import org.birdview.BVTaskService
import org.birdview.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Controller
class BVWebPageController(
        private val taskService: BVTaskService
) {
    class ReportLink(val reportUrl:String, val reportName:String)
    @GetMapping("/")
    fun hello(model: Model,
              @RequestParam(value = "user", required = false) user: String?,
              @RequestParam(value = "report", required = false) report: String?
    ): String? {
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val tsRequest = buildTSRequest(user, report)
        val docs = taskService.getDocuments(tsRequest)
                .map(BVDocumentViewFactory::create)
        model.asMap().putAll(mapOf(
                "reportLinks" to ReportType.values()
                        .map {
                            ReportLink(
                                    reportUrl = reportUrl(it, tsRequest, baseUrl),
                                    reportName = it.name.toLowerCase().capitalize())
                        },
                "user" to tsRequest.userFilters.firstOrNull(),
                "docs" to docs,
                "baseURL" to baseUrl,
                "reportPath" to "report-${tsRequest.reportType}.ftl",
                "format" to getFormat(tsRequest.reportType)))
        return "report"
    }

    private fun reportUrl(reportType: ReportType, tsRequest: BVDocumentFilter, baseUrl: String): String {
        return "${baseUrl}?report=${reportType.name.toLowerCase()}" +
                (tsRequest.userFilters
                        .firstOrNull { it.role == UserRole.IMPLEMENTOR }
                        ?.userAlias
                        ?.let { "&user=${it}" } ?: "")
    }


    private fun buildTSRequest(user: String?, report: String?) : BVDocumentFilter {
        val sourceType = null
        val reportType = report
                ?.toUpperCase()
                ?.let { ReportType.valueOf(it) }
                ?: ReportType.LAST_DAY
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        return when(reportType) {
            ReportType.LAST_DAY -> {
                val minusDays:Long = when(today.dayOfWeek) {
                    DayOfWeek.MONDAY -> 3L
                    DayOfWeek.SUNDAY -> 2L
                    else -> 1L
                }
                BVDocumentFilter(
                        reportType = reportType,
                        grouping = false,
                        updatedPeriod = TimeIntervalFilter(after = today.minusDays(minusDays)),
                        userFilters = listOf(UserFilter( userAlias = user, role = UserRole.IMPLEMENTOR),
                                UserFilter( userAlias = user, role = UserRole.CREATOR)),
                        sourceType = sourceType)
            }
            ReportType.PLANNED -> BVDocumentFilter(
                    reportType = reportType,
                    grouping = true,
                    updatedPeriod = TimeIntervalFilter(),
                    userFilters = listOf(UserFilter( userAlias = user, role = UserRole.IMPLEMENTOR)),
                    sourceType = sourceType)
            ReportType.WORKED -> BVDocumentFilter(
                    reportType = reportType,
                    grouping = true,
                    updatedPeriod = TimeIntervalFilter(after = today.minusDays(10)),
                    userFilters = listOf(UserFilter( userAlias = user, role = UserRole.IMPLEMENTOR)),
                    sourceType = sourceType)
        }
    }

    private fun getFormat(reportType: ReportType): String = when(reportType) {
        ReportType.LAST_DAY -> "brief"
        else -> "long"
    }
}