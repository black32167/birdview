package org.birdview.source.gdrive

import org.birdview.analysis.*
import org.birdview.model.BVDocumentRef
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVSessionDocumentConsumer
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.source.gdrive.model.GDriveFile
import org.birdview.source.gdrive.model.GDriveUser
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVGDriveConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
open class GDriveTaskService(
        private val client: GDriveClient,
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val tokenStorage: OAuthRefreshTokenStorage,
        private val gDriveQueryBuilder: GDriveQueryBuilder
) : BVTaskSource {
    private val log = LoggerFactory.getLogger(GDriveTaskService::class.java)
    companion object {
        private const val GDRIVE_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

    override fun getTasks(bvUser: String, updatedPeriod: TimeIntervalFilter, sourceConfig: BVAbstractSourceConfig, chunkConsumer: BVSessionDocumentConsumer) {
        try {
            sourceSecretsStorage.getConfigOfType(BVGDriveConfig::class.java)
                    ?.also { config ->
                        client.getFiles(
                            config,
                            gDriveQueryBuilder.getQuery(bvUser, updatedPeriod, config.sourceName)) { files ->
                                chunkConsumer.consume(files.map { file -> toBVDocument(file, config) })
                            }
                    }
        } catch (e: Exception) {
            log.error("", e)
        }
    }

    override fun getType() = SourceType.GDRIVE

    override fun isAuthenticated(sourceName: String): Boolean =
        sourceSecretsStorage.getConfigByName(sourceName, BVGDriveConfig::class.java)
                ?.let { config -> tokenStorage.hasToken(config) }
                ?: false

    private fun toBVDocument(file: GDriveFile, config: BVGDriveConfig) = try {
        BVDocument(
                ids = setOf(
                        BVDocumentId(id = file.id),
                        BVDocumentId(id = file.webViewLink)
                ),
                title = BVFilters.removeIdsFromText(file.name),
                key = "open",
                updated = parseDate(file.modifiedTime),
                httpUrl = file.webViewLink,
                users = extractUsers(file, config),
                refs = BVFilters.filterRefsFromText(file.name).map { BVDocumentRef(it) },
                status = BVDocumentStatus.PROGRESS,
                sourceType = getType(),
                operations = extractOperations(file, config.sourceName),
                sourceName = config.sourceName
        )
    } catch (e:Exception) {
        log.error("", e)
        throw e
    }

    private fun extractOperations(file: GDriveFile, sourceName:String): List<BVDocumentOperation> {
        val lastModification = file.lastModifyingUser?.let { user->
            BVDocumentOperation(
                    description = "edit",
                    author = user.emailAddress ?: "",
                    authorDisplayName = user.displayName,
                    created = parseDate(file.modifiedTime),
                    sourceName = sourceName,
                    type = BVDocumentOperationType.UPDATE
            )
        }
        return listOfNotNull(lastModification)
    }

    private fun extractUsers(file: GDriveFile, config: BVGDriveConfig): List<BVDocumentUser> {
        val users = mutableListOf<BVDocumentUser>()
        users += file.owners
                .mapNotNull { user -> mapDocumentUser(user, config.sourceName, UserRole.IMPLEMENTOR) }.toMutableList()
        users += file.owners.mapNotNull { user -> mapDocumentUser(user, config.sourceName, UserRole.IMPLEMENTOR) }.toMutableList()
        mapDocumentUser(file.sharingUser, config.sourceName, UserRole.IMPLEMENTOR) ?.also { users.add(it) }
        if(file.modifiedByMe) {
            mapDocumentUser(config.user, config.sourceName, UserRole.IMPLEMENTOR) ?.also { users.add(it) }
        }
        return users
    }

    private fun mapDocumentUser(user: GDriveUser?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            mapDocumentUser(user ?.emailAddress, sourceName, userRole)
    private fun mapDocumentUser(email: String?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            email ?.let { BVDocumentUser(userName = it, role = userRole, sourceName = sourceName) }

    private fun parseDate(dateString: String) = BVDateTimeUtils.parse(dateString, GDRIVE_DATETIME_PATTERN)
}