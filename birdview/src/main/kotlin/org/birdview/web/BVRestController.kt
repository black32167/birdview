package org.birdview.web

import org.birdview.BVTaskService
import org.birdview.model.*
import org.birdview.web.source.BVDocumentView
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@RestController()
@RequestMapping("/rest")
class BVRestController(
        private val taskService: BVTaskService
) {
    class ReportLink(val reportUrl:String, val reportName:String)

    @GetMapping("/report")
    fun report(
            @RequestParam(value = "user", required = false) user: String?,
            @RequestParam(value = "report", required = false) report: String?
    ): List<BVDocumentView> {
        val tsRequest = buildTSRequest(user, report)
        val docs = taskService.getDocuments(tsRequest)
                .map(BVDocumentViewFactory::create)
        return docs
    }

    class DocumentRequest(
            val user: String?,
            val reportType: ReportType,
            val daysBack: Long,
            val sourceType: String?,
            val userRole: UserRole = UserRole.CREATOR
    )
    @GetMapping("/documents")
    fun documents(
            documentRequest: DocumentRequest
    ): List<BVDocumentView> {
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val user = documentRequest.user
        val tsRequest = BVDocumentFilter(
                reportType = documentRequest.reportType,
                grouping = true,
                updatedPeriod = TimeIntervalFilter(after = today.minusDays(documentRequest.daysBack)),
                userFilter = UserFilter( userAlias = user, role = documentRequest.userRole),
                sourceType = documentRequest.sourceType)
        val docs = taskService.getDocuments(tsRequest)
                .map(BVDocumentViewFactory::create)
        return docs
    }

    @PostMapping("/documents/reindex")
    fun reindexDocuments() {
        taskService.invalidateCache()
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
}