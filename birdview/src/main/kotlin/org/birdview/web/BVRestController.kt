package org.birdview.web

import org.birdview.BVTaskService
import org.birdview.model.*
import org.birdview.security.UserContext
import org.birdview.web.secrets.BVDocumentView
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@RestController()
@RequestMapping("/rest")
class BVRestController(
        private val taskService: BVTaskService
) {
    class DocumentRequest(
            val user: String?,
            val reportType: ReportType,
            val daysBack: Long,
            val sourceType: String?,
            val userRole: UserRole = UserRole.CREATOR,
            var representationType: RepresentationType
    )
    @GetMapping("documents")
    fun documents(
            documentRequest: DocumentRequest
    ): List<BVDocumentView> {
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val user = documentRequest.user.takeUnless { it == "" } ?: UserContext.getUserName()
        val tsRequest = BVDocumentFilter(
                reportType = documentRequest.reportType,
                grouping = true,
                updatedPeriod = TimeIntervalFilter(after = today.minusDays(documentRequest.daysBack)),
                userFilter = UserFilter( userAlias = user, role = documentRequest.userRole),
                sourceType = documentRequest.sourceType,
                representationType = documentRequest.representationType)
        val docs = taskService.getDocuments(tsRequest)
                .map(BVDocumentViewFactory::create)
        return docs
    }

    @GetMapping("documents/reindex")
    fun reindexDocuments() {
        taskService.invalidateCache()
    }
}