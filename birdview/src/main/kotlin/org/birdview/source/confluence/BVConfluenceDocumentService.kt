package org.birdview.source.confluence

import org.birdview.analysis.*
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.confluence.model.ConfluenceSearchItemContent
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVConfluenceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.storage.BVUserSourceStorage
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class BVConfluenceDocumentService (
    private val client: ConfluenceClient,
    private val sourceSecretsStorage: BVSourceSecretsStorage,
    private val userSourceStorage: BVUserSourceStorage
): BVTaskSource {
    private val CONFLUENCE_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    override fun getTasks(bvUser: String, updatedPeriod: TimeIntervalFilter, sourceConfig: BVAbstractSourceConfig, chunkConsumer: BVSessionDocumentConsumer) {
        val confluenceConfig = sourceConfig as BVConfluenceConfig
        val confluenceUser = userSourceStorage.getSourceProfile(bvUser, sourceName = sourceConfig.sourceName).sourceUserName
        val cql =
            "type IN (page,comment) AND " +
                "(contributor=\"${confluenceUser}\" OR creator=\"${confluenceUser}\")" +
                (updatedPeriod.after?.let { after -> " AND lastmodified>\"${formatDate(after)}\" " } ?: "") +
                (updatedPeriod.before?.let { before -> " AND lastmodified<=\"${formatDate(before)}\" " } ?: "") +
                " ORDER BY lastmodified DESC"
        client.findDocuments(confluenceConfig, cql) { confluenceDocuments ->
            // Feed loaded pages
            confluenceDocuments
                .filter { it.content?.type == "page" }
                .map { mapDocument(it.content!!, confluenceConfig = sourceConfig, bvUser = bvUser) }
                .also (chunkConsumer::consume)

            // Load parent pages for comments if not yet
            confluenceDocuments
                .filter { it.content?.type == "comment" }
                .map {
                    confluenceConfig.baseUrl + it.content!!._expandable.container
                }
                .distinct()
                .filter { pageUrl->!chunkConsumer.isConsumed(pageUrl) }
                .map { pageUrl ->
                    client.loadPage(confluenceConfig, pageUrl)
                }
                .map { mapDocument(it, confluenceConfig = sourceConfig, bvUser = bvUser) }
                .also (chunkConsumer::consume)
        }
    }

    private fun mapDocument(confluenceDocument: ConfluenceSearchItemContent, confluenceConfig:BVConfluenceConfig, bvUser: String): BVDocument {
        val sourceName = confluenceConfig.sourceName
        val confluenceUser = userSourceStorage.getSourceProfile(bvUser, sourceName = sourceName).sourceUserName
        val lastModified = parseDate(confluenceDocument.version._when)
        val docUrl = "${confluenceConfig.baseUrl}/${confluenceDocument._links.webui.trimStart('/')}"

        // load document comments
        val comments:List<ConfluenceSearchItemContent> = client.loadComments(confluenceConfig, confluenceDocument.id)

        return BVDocument(
                ids = setOf(
                    BVDocumentId(id = docUrl),
                    BVDocumentId("https://canvadev.atlassian.net/wiki/pages/viewpage.action?pageId=${confluenceDocument.id}")),
                title = confluenceDocument.title,
                key = "open",
                body = confluenceDocument._expandable.body ?: "",
                updated = lastModified,
                created = null,
                httpUrl = docUrl,
                users = listOf(BVDocumentUser(userName = confluenceUser, sourceName = sourceName, role = UserRole.IMPLEMENTOR)), //TODO
                refs = extractRefs(confluenceDocument), // TODO
                status = BVDocumentStatus.PROGRESS,
                operations = extractOperations(confluenceDocument, comments, sourceName),
                sourceType = getType(), //TODO: will overwrite other users
                priority = Priority.LOW,
                sourceName = sourceName
        )
    }

    private fun extractRefs(confluenceDocument: ConfluenceSearchItemContent): List<BVDocumentRef> {
        val idsFromExcerpt = confluenceDocument._expandable.body ?.let { BVFilters.filterRefsFromText(it) } ?: emptySet()
        val idsFromTitle = BVFilters.filterRefsFromText(confluenceDocument.title)
        return (idsFromExcerpt + idsFromTitle).map { BVDocumentRef(it) }
    }

    private fun extractOperations(
        confluenceDocument: ConfluenceSearchItemContent,
        comments: List<ConfluenceSearchItemContent>,
        sourceName: String
    ): List<BVDocumentOperation> {
        val operations = mutableListOf<BVDocumentOperation>()

        val history = confluenceDocument.history

        comments
            .map { commentContent-> BVDocumentOperation(
                description = "",
                author = commentContent.version.by.accountId,
                created = parseDate(commentContent.version._when),
                sourceName = sourceName,
                type = BVDocumentOperationType.COMMENT
            ) }
            .forEach (operations::add)

        val createdBy = history.createdBy
        if (createdBy != null) {
            operations.add(BVDocumentOperation(
                description = "",
                author = history.createdBy.accountId,
                authorDisplayName = history.createdBy.run { email ?: displayName },
                created = parseDate(history.createdDate),
                sourceName = sourceName,
                type = BVDocumentOperationType.UPDATE
            ))
        }

        history.contributors.publishers.users
            .map { user ->
                val contributorAccountId = user.accountId
                BVDocumentOperation(
                    description = "",
                    author = contributorAccountId,
                    authorDisplayName = user.run { email ?: displayName },
                    created = parseDate(confluenceDocument.version._when),
                    sourceName = sourceName,
                    type = BVDocumentOperationType.UPDATE
                )
            }
            .forEach (operations::add)

        return operations
    }

    override fun getType(): SourceType = SourceType.CONFLUENCE

    override fun isAuthenticated(sourceName: String): Boolean =
            sourceSecretsStorage.getConfigByName(sourceName, BVConfluenceConfig::class.java) != null

    private fun formatDate(date: OffsetDateTime) =
            date.atZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00"))

    private fun parseDate(dateTimeString:String?) =
            BVDateTimeUtils.parse(dateTimeString, CONFLUENCE_DATETIME_PATTERN)
}