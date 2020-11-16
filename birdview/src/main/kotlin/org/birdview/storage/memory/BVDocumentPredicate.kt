package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentOperationType
import org.birdview.model.*
import org.birdview.storage.BVUserSourceStorage
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.chrono.ChronoZonedDateTime
import java.util.*
import javax.inject.Named

@Named
class BVDocumentPredicate(
        private val userSourceStorage: BVUserSourceStorage
) {
    private val log = LoggerFactory.getLogger(BVDocumentFilter::class.java)

    fun test(doc: BVDocument, filter: BVDocumentFilter): Boolean {
        if(filter.sourceType != "" && filter.sourceType?.let { filterSource -> doc.ids.any { it.sourceName == filterSource }} == false) {
            return false
        }

        val docUpdated = if (filter.reportType == ReportType.WORKED)
            getLastUserUpdateDate(doc, filter.userFilter)
        else {
            if (isDocEverModifiedByUser(doc, filter.userFilter.userAlias))
                toInstant(doc.updated)
            else null
        }

        if (filter.updatedPeriod.after != null) {
            if (docUpdated == null || filter.updatedPeriod.after > docUpdated) {
                log.trace("Filtering out doc #{} (updatedPeriod.after)", doc.title)
                return false
            }
        }

        if (filter.updatedPeriod.before != null) {
            if (docUpdated == null || filter.updatedPeriod.before <= docUpdated) {
                log.trace("Filtering out doc #{} (updatedPeriod.before)", doc.title)
                return false
            }
        }

        val inferredDocStatus = doc.status
        val targetDocumentStatuses = getTargetDocStatuses(filter.reportType)
        if (!targetDocumentStatuses.contains(inferredDocStatus)) {
            log.trace("Filtering out doc #{} (inferredDocStatus)", doc.title)
            return false
        }

        if (!targetDocumentStatuses.contains(doc.status)) {
            log.trace("Filtering out doc #{} (doc.status)", doc.title)
            return false
        }


        log.trace("Including doc #{}", doc.title)
        return true
    }

    private fun resolveUserName(bvUser:String, sourceName: String) =
            userSourceStorage.getSourceProfile(bvUser, sourceName).sourceUserName

    private fun getLastUserUpdateDate(doc: BVDocument, userFilter: UserFilter): ChronoZonedDateTime<*>? {
        val date = getLastUserOperation(doc, userFilter.userAlias) ?.created
               // ?: getDocDate(doc)

        return toInstant(date)
    }

    private fun toInstant(date: Date?): ChronoZonedDateTime<*>? =
        date?.toInstant()?.atZone(ZoneId.of("UTC"))

    private fun getLastOperationForRoles(doc: BVDocument, bvUser:String, roles: List<UserRole>): BVDocumentOperation? {
//        if (!userFilter.roles.contains(UserRole.IMPLEMENTOR)) {
//            return null
//        }
        return doc.lastOperations.firstOrNull { operation ->
            resolveUserName(bvUser, operation.sourceName)
                    ?.let { sourceFilteringUserName->
                        sourceFilteringUserName == operation.author && mapOperationTypeToRole(operation.type).any { roles.contains(it) }
                    } ?: false
        }
    }

    private fun isDocEverModifiedByUser(doc: BVDocument, bvUser: String): Boolean {
        val hasFilteredUser = doc.users.any { docUser ->
            resolveUserName(bvUser, docUser.sourceName)
                    ?.let { sourceFilteringUserName ->
                        sourceFilteringUserName == docUser.userName
                    } ?: false
        }

        return if (hasFilteredUser)
            return true
        else
            getLastUserOperation(doc, bvUser) != null
    }

    private fun getLastUserOperation(doc: BVDocument, bvUser:String): BVDocumentOperation? {
//        if (!userFilter.roles.contains(UserRole.IMPLEMENTOR)) {
//            return null
//        }
        return doc.lastOperations.firstOrNull { operation ->
            resolveUserName(bvUser, operation.sourceName)
                    ?.let { sourceFilteringUserName->
                        sourceFilteringUserName == operation.author
                    } ?: false
        }
    }

    private fun mapOperationTypeToRole(type: BVDocumentOperationType): Set<UserRole> =
            when (type) {
                BVDocumentOperationType.COLLABORATE -> setOf(UserRole.IMPLEMENTOR)
                else -> setOf(UserRole.WATCHER)
            }

    private fun getDocDate(doc: BVDocument): Date? =
            if(doc.closed != null && doc.closed < doc.updated) {
                doc.closed //doc.closed
            } else {
                doc.updated
            }

    private fun getTargetDocStatuses(reportType: ReportType) = when (reportType) {
        ReportType.WORKED -> listOf(BVDocumentStatus.DONE, BVDocumentStatus.PROGRESS)
        ReportType.PLANNED -> listOf(BVDocumentStatus.PROGRESS, BVDocumentStatus.PLANNED, BVDocumentStatus.BACKLOG)
    }

}