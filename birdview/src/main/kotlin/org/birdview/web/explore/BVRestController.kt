package org.birdview.web.explore

import org.birdview.BVTaskService
import org.birdview.model.*
import org.birdview.security.UserContext
import org.birdview.storage.BVDocumentStorage
import org.birdview.user.BVUserDataUpdater
import org.birdview.web.explore.model.BVDocumentViewTreeNode
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@RestController()
@RequestMapping("/rest")
class BVRestController(
        private val taskService: BVTaskService,
        private val userUpdater: BVUserDataUpdater,
        private val documentStorage: BVDocumentStorage
) {
    class DocumentRequest(
            val user: String?,
            val reportType: ReportType,
            val daysBack: Long,
            val sourceType: String?,
            val userRole: UserRole?,
            var representationType: RepresentationType
    )
    @GetMapping("documents")
    fun documents(
            documentRequest: DocumentRequest
    ): Collection<BVDocumentViewTreeNode> {
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val user = documentRequest.user.takeUnless { it == "" } ?: UserContext.getUserName()
        val userRoles = documentRequest.userRole?.let { listOf(it) } ?: inferRolesFromReportType(documentRequest.reportType)
        val tsRequest = BVDocumentFilter(
                reportType = documentRequest.reportType,
                grouping = true,
                updatedPeriod = TimeIntervalFilter(after = today.minusDays(documentRequest.daysBack)),
                userFilter = UserFilter( userAlias = user, roles = userRoles),
                sourceType = documentRequest.sourceType,
                representationType = documentRequest.representationType)
  //      userUpdater.waitForUserUpdated(tsRequest.userFilter.userAlias)
        val docs = taskService.getDocuments(tsRequest)
        val docViews = DocumentTreeBuilder.buildTree(docs, documentStorage, documentRequest.reportType).toMutableList()

        if (documentRequest.representationType == RepresentationType.LIST) {
            docViews.forEach(HierarchyOptimizer::optimizeHierarchy)
        }
        return docViews
    }

    private fun inferRolesFromReportType(reportType: ReportType): List<UserRole> = when(reportType) {
        ReportType.PLANNED -> listOf(UserRole.WATCHER, UserRole.IMPLEMENTOR, UserRole.CREATOR)
        ReportType.WORKED -> listOf(UserRole.IMPLEMENTOR)
    }

    @GetMapping("documents/reindex")
    fun reindexDocuments() {
        userUpdater.refreshUser(UserContext.getUserName())
    }
}