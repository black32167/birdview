package org.birdview.web.explore

import org.birdview.BVTaskService
import org.birdview.analysis.Priority
import org.birdview.model.*
import org.birdview.security.UserContext
import org.birdview.source.SourceType
import org.birdview.storage.BVDocumentStorage
import org.birdview.user.BVUserDataUpdater
import org.birdview.user.BVUserLog
import org.birdview.web.explore.model.BVDocumentView
import org.birdview.web.explore.model.BVUserLogEntry
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
        private val documentStorage: BVDocumentStorage,
        private val userLog: BVUserLog
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
        val topNodes = ArrayList<BVDocumentViewTreeNode>()
        for (role in userRoles) {
            val docFilter = BVDocumentFilter(
                    docStatuses = getTargetDocStatuses(documentRequest.reportType),
                    grouping = true,
                    updatedPeriod = TimeIntervalFilter(after = today.minusDays(documentRequest.daysBack)),
                    userFilter = UserFilter(userAlias = user, role = role),
                    sourceType = documentRequest.sourceType,
                    representationType = documentRequest.representationType)
            //      userUpdater.waitForUserUpdated(tsRequest.userFilter.userAlias)
            val docs = taskService.getDocuments(docFilter)
            val docViews = DocumentTreeBuilder.buildTree(docs, documentStorage, documentRequest.reportType).toMutableList()

            if (documentRequest.representationType == RepresentationType.LIST) {
                docViews.forEach(HierarchyOptimizer::optimizeHierarchy)
            }

            if (docViews.isNotEmpty()) {
                val roleNode = BVDocumentViewTreeNode(
                        doc = BVDocumentView(role.name, listOf(), role.name, null, "", "", "", "", null, Priority.NORMAL),
                        sourceType = SourceType.NONE)
                roleNode.subNodes.addAll(docViews)
                topNodes += roleNode
            }
        }
        return topNodes
    }

    @GetMapping("documents/reindex")
    fun reindexDocuments() {
        userUpdater.requestUserRefresh(UserContext.getUserName())
    }

    @GetMapping("documents/status")
    fun getDocumentUpdateStatus(): List<BVUserLogEntry> =
        userLog.getUserLog(UserContext.getUserName())

    private fun inferRolesFromReportType(reportType: ReportType): List<UserRole> = when(reportType) {
        ReportType.PLANNED -> listOf(UserRole.WATCHER, UserRole.IMPLEMENTOR, UserRole.CREATOR)
        ReportType.WORKED -> listOf(UserRole.IMPLEMENTOR)
    }

    private fun getTargetDocStatuses(reportType: ReportType) = when (reportType) {
        ReportType.WORKED -> listOf(BVDocumentStatus.DONE, BVDocumentStatus.PROGRESS)
        ReportType.PLANNED -> listOf(BVDocumentStatus.PROGRESS, BVDocumentStatus.PLANNED, BVDocumentStatus.BACKLOG)
    }
}