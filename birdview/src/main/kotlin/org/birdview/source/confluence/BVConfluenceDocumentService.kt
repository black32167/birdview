package org.birdview.source.confluence

import org.birdview.analysis.*
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.confluence.model.ConfluenceSearchItem
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVConfluenceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserSourceStorage
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class BVConfluenceDocumentService (
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val userSourceStorage: BVUserSourceStorage
): BVTaskSource {
    // 2020-03-10T05:43:13.000Z
    private val CONFLUENCE_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    override fun getTasks(bvUser: String, updatedPeriod: TimeIntervalFilter, sourceConfig: BVAbstractSourceConfig, chunkConsumer: (List<BVDocument>) -> Unit) {
        val confluenceConfig = sourceConfig as BVConfluenceConfig
        val client = ConfluenceClient(config = confluenceConfig)
        val confluenceUser = userSourceStorage.getSourceProfile(bvUser, sourceName = sourceConfig.sourceName).sourceUserName
        val cql = "type=page AND contributor=\"${confluenceUser}\"" +
                (updatedPeriod.after?.let { after -> " AND lastmodified >= \"${formatDate(after)}\" " } ?: "") +
                " ORDER BY lastmodified DESC"
        client.findDocuments(cql) { confluenceDocuments ->
            chunkConsumer(confluenceDocuments.map { mapDocument(it, confluenceConfig = sourceConfig, bvUser = bvUser) })
        }
    }

    private fun mapDocument(confluenceDocument: ConfluenceSearchItem, confluenceConfig:BVConfluenceConfig, bvUser: String): BVDocument {
        val sourceName = confluenceConfig.sourceName
        val confluenceUser = userSourceStorage.getSourceProfile(bvUser, sourceName = sourceName).sourceUserName
        val lastModified = parseDate(confluenceDocument.lastModified)
        val docUrl = "${confluenceConfig.baseUrl}/${confluenceDocument.url.trimStart('/')}"
        return BVDocument(
                ids = setOf(BVDocumentId(id = docUrl)),
                title = confluenceDocument.title,
                key = "open",
                body = confluenceDocument.excerpt ?: "",
                updated = lastModified,
                created = null,
                httpUrl = docUrl,
                users = listOf(BVDocumentUser(userName = confluenceUser, sourceName = sourceName, role = UserRole.IMPLEMENTOR)), //TODO
                refs = extractRefs(confluenceDocument), // TODO
                status = BVDocumentStatus.PROGRESS,
                operations = extractOperations(confluenceDocument, sourceName),
                sourceType = getType(), //TODO: will overwrite other users
                priority = Priority.LOW,
                sourceName = sourceName
        )
    }

    private fun extractRefs(confluenceDocument: ConfluenceSearchItem): List<BVDocumentRef> {
        val idsFromExcerpt = confluenceDocument.excerpt ?.let { BVFilters.filterRefsFromText(it) } ?: emptySet()
        val idsFromTitle = BVFilters.filterRefsFromText(confluenceDocument.title)
        return (idsFromExcerpt + idsFromTitle).map { BVDocumentRef(it) }
    }

    private fun extractOperations(confluenceDocument: ConfluenceSearchItem, sourceName: String): List<BVDocumentOperation> {
        val history = confluenceDocument.content.history
        val creationOperation = BVDocumentOperation(
                description = "",
                author = history.createdBy.accountId,
                authorDisplayName = history.createdBy.run { email?:displayName },
                created = parseDate(history.createdDate),
                sourceName = sourceName,
                type = BVDocumentOperationType.UPDATE
        )
        val modificationOperations = history.contributors.publishers.users.map { user ->
            val contributorAccountId = user.accountId
            BVDocumentOperation(
                    description = "",
                    author = contributorAccountId,
                    authorDisplayName = user.run { email?:displayName },
                    created = parseDate(confluenceDocument.lastModified),
                    sourceName = sourceName,
                    type = BVDocumentOperationType.UPDATE
            )
        }
        return modificationOperations + creationOperation
    }

    override fun getType(): SourceType = SourceType.CONFLUENCE

    override fun isAuthenticated(sourceName: String): Boolean =
            sourceSecretsStorage.getConfigByName(sourceName, BVConfluenceConfig::class.java) != null

    private fun formatDate(date: ZonedDateTime) =
            date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    private fun parseDate(dateTimeString:String?) =
            BVDateTimeUtils.parse(dateTimeString, CONFLUENCE_DATETIME_PATTERN)
}