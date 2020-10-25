package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentOperationType
import org.birdview.model.*
import org.birdview.storage.BVSourceUserNameResolver
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.chrono.ChronoZonedDateTime
import java.util.*
import java.util.function.Predicate

class BVDocumentPredicate(
        private val filter: BVDocumentFilter,
        private val sourceUserNameResolver: BVSourceUserNameResolver
): Predicate<BVDocument> {
    private val log = LoggerFactory.getLogger(BVDocumentFilter::class.java)

    override fun test(doc: BVDocument): Boolean {
        if(filter.sourceType != "" && filter.sourceType?.let { filterSource -> doc.ids.any { it.sourceName == filterSource }} == false) {
            return false
        }

        val docUpdated = inferDocUpdated(doc, filter.userFilter)
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

        val inferredDocStatus = doc.status//inferDocStatus(doc)
        val targetDocumentStatuses = getTargetDocStatuses(filter.reportType)
        if (!targetDocumentStatuses.contains(inferredDocStatus)) {
            log.trace("Filtering out doc #{} (inferredDocStatus)", doc.title)
            return false
        }

        if (!targetDocumentStatuses.contains(doc.status)) {
            log.trace("Filtering out doc #{} (doc.status)", doc.title)
            return false
        }

        val userFilter = filter.userFilter
        val hasFilteredUser = doc.users.any { docUser ->
            sourceUserNameResolver.resolve(filter.userFilter.userAlias, docUser.sourceName)
                    ?.let { sourceFilteringUserName->
                        sourceFilteringUserName == docUser.userName && userFilter.roles.contains(docUser.role)
                    } ?: false
        }
        if (!hasFilteredUser) {
            log.trace("Filtering out doc #{} (hasFilteredUser)", doc.title)
            return false
        }

        log.trace("Including doc #{}", doc.title)
        return true
    }

    private fun inferDocUpdated(doc: BVDocument, userFilter: UserFilter): ChronoZonedDateTime<*>? {
        val date = getLastOperationDate(doc, userFilter)
                ?: getDocDate(doc)

        return date?.toInstant()?.atZone(ZoneId.of("UTC"))
    }

    private  fun getLastOperationDate(doc: BVDocument, userFilter: UserFilter): Date? =
            getLastOperation(doc, userFilter) ?.created

    private fun getLastOperation(doc: BVDocument, userFilter: UserFilter): BVDocumentOperation? {
        if (!userFilter.roles.contains(UserRole.IMPLEMENTOR)) {
            return null
        }
        return doc.lastOperations.firstOrNull { operation ->
            sourceUserNameResolver.resolve(filter.userFilter.userAlias, operation.sourceName)
                    ?.let { sourceFilteringUserName->
                        sourceFilteringUserName == operation.author && mapOperationTypeToRole(operation.type).any { userFilter.roles.contains(it) }
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
//
//    private fun inferDocStatus(doc: BVDocument): BVDocumentStatus? {
//        val parentStatuses = doc.refsIds
//                .map { key -> getDocByStringKey(key)?.status }
//        if (parentStatuses.isNotEmpty() && parentStatuses.all { it == BVDocumentStatus.DONE }) {
//            return BVDocumentStatus.DONE
//        }
//        return doc.status
//    }
//
//    private fun getDocByStringKey(key: String): BVDocument? =
//            docsMap.values.find { doc -> doc.ids.any { it.id == key } }

    private fun getTargetDocStatuses(reportType: ReportType) = when (reportType) {
        ReportType.WORKED -> listOf(BVDocumentStatus.DONE, BVDocumentStatus.PROGRESS)
        ReportType.PLANNED -> listOf(BVDocumentStatus.PROGRESS, BVDocumentStatus.PLANNED, BVDocumentStatus.BACKLOG)
    }

}