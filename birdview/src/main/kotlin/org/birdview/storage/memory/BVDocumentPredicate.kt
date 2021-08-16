package org.birdview.storage.memory

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentOperation
import org.birdview.analysis.BVDocumentOperationType
import org.birdview.model.BVDocumentFilter
import org.birdview.model.UserFilter
import org.birdview.model.UserRole
import org.birdview.storage.BVUserSourceConfigStorage
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import javax.inject.Named

@Named
open class BVDocumentPredicate(
        private val userSourceStorage: BVUserSourceConfigStorage
) {
    private val log = LoggerFactory.getLogger(BVDocumentFilter::class.java)

    fun test(doc: BVDocument, filter: BVDocumentFilter): Boolean {
        if(filter.sourceType != "" && filter.sourceType?.let { filterSource -> doc.sourceName == filterSource } == false) {
            return false
        }

        val docUpdated = when (filter.userFilter.role) {
            UserRole.IMPLEMENTOR ->
                getLastUserUpdateDate(doc, filter.userFilter, BVDocumentOperationType.UPDATE)
            UserRole.COMMENTER ->
                getLastUserUpdateDate(doc, filter.userFilter, BVDocumentOperationType.COMMENT)
            UserRole.WATCHER ->
                doc.updated.takeIf { isDocEverModifiedByUser(doc, filter.userFilter.userAlias) }
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
        val targetDocumentStatuses = filter.docStatuses
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
            userSourceStorage.getSource(bvUser, sourceName)?.sourceUserName

    private fun getLastUserUpdateDate(doc: BVDocument, userFilter: UserFilter, operationType: BVDocumentOperationType): OffsetDateTime? {
        val date = getLastUserOperation(doc, userFilter.userAlias, operationType) ?.created
        return date
    }

//    private fun toInstant(date: Date?): OffsetDateTime? =
//        date?.toInstant()?.atZone(ZoneId.of("UTC"))

    private fun isDocEverModifiedByUser(doc: BVDocument, bvUser: String): Boolean {
        val hasFilteredUser = doc.users.any { docUser ->
            resolveUserName(bvUser, docUser.sourceName) == docUser.userName
        }

        return if (hasFilteredUser)
            return true
        else
            getLastUserOperation(doc, bvUser) != null
    }

    private fun getLastUserOperation(doc: BVDocument, bvUser:String, operationType: BVDocumentOperationType? = null): BVDocumentOperation? {
        return doc.operations
            .sortedByDescending { it.created }
            .firstOrNull { operation ->
                resolveUserName(bvUser, operation.sourceName)
                        .let { sourceFilteringUserName->
                            sourceFilteringUserName == operation.author && operationType == operation.type
                        }
        }
    }
}