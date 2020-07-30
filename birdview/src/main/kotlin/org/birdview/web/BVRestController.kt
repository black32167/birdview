package org.birdview.web

import org.birdview.BVTaskService
import org.birdview.model.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@RestController()
@RequestMapping("/rest")
class BVRestController(
        private val taskService: BVTaskService
) {
    class ReportLink(val reportUrl:String, val reportName:String)

    @RequestMapping("/report")
    fun report(
            @RequestParam(value = "user", required = false) user: String?,
            @RequestParam(value = "report", required = false) report: String?
    ): List<BVDocumentView> {
        val tsRequest = buildTSRequest(user, report)
        val docs = taskService.getDocuments(tsRequest)
                .map(BVDocumentViewFactory::create)
        return docs
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
}