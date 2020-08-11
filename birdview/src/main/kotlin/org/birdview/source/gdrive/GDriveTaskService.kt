package org.birdview.source.gdrive

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.analysis.BVDocumentUser
import org.birdview.config.BVGDriveConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVUsersConfigProvider
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.model.UserRole
import org.birdview.source.BVTaskSource
import org.birdview.source.gdrive.model.GDriveFile
import org.birdview.source.gdrive.model.GDriveUser
import org.birdview.utils.BVDateTimeUtils
import org.birdview.utils.BVFilters
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
open class GDriveTaskService(
        private val clientProvider: GDriveClientProvider,
        private val bvConfigProvider: BVSourcesConfigProvider,
        private val gDriveQueryBuilder: GDriveQueryBuilder,
        private val bvUsersConfigProvider: BVUsersConfigProvider
) : BVTaskSource {
    private val log = LoggerFactory.getLogger(GDriveTaskService::class.java)
    companion object {
        val GDRIVE_FILE_TYPE = "gDriveFile"
        private const val GDRIVE_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

    override fun getTasks(user: String?, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit) {
        try {
            bvConfigProvider.getConfigOfType(BVGDriveConfig::class.java)
                    ?.also { config ->
                        clientProvider.getGoogleApiClient(config)
                                .getFiles(gDriveQueryBuilder.getQuery(user, updatedPeriod, config.sourceName)) { files ->
                                    chunkConsumer.invoke(files.map { file -> toBVDocument(file, config) })
                                }
                    }
        } catch (e: Exception) {
            log.error("", e)
        }
    }

    override fun getType() = "gdrive"

    override fun isAuthenticated(sourceName: String): Boolean =
        bvConfigProvider.getConfigByName(sourceName, BVGDriveConfig::class.java)
                ?.let { config -> clientProvider.isAuthenticated(config) }
                ?: false

    private fun toBVDocument(file: GDriveFile, config: BVGDriveConfig) = try {
        BVDocument(
                ids = setOf(BVDocumentId(id = file.id, type = GDRIVE_FILE_TYPE, sourceName = config.sourceName)),
                refsIds = BVFilters.filterIdsFromText(file.name),
                title = file.name,
                updated = parseDate(file.modifiedTime),
                httpUrl = file.webViewLink,
                status = BVDocumentStatus.PROGRESS,
                key = "open",
                users = extractUsers(file, config)
        )
    } catch (e:Exception) {
        log.error("", e)
        throw e
    }

    private fun extractUsers(file: GDriveFile, config: BVGDriveConfig): List<BVDocumentUser> =
            listOf(UserRole.CREATOR, UserRole.IMPLEMENTOR).flatMap { userRole ->
                file.owners.mapNotNull { user -> mapDocumentUser(user, config.sourceName, userRole) }
            } + listOfNotNull(mapDocumentUser(file.sharingUser, config.sourceName, UserRole.IMPLEMENTOR))

    private fun mapDocumentUser(user: GDriveUser?, sourceName: String, userRole: UserRole): BVDocumentUser? =
            bvUsersConfigProvider.getUser(user?.emailAddress, sourceName, userRole)

    private fun parseDate(dateString: String) = BVDateTimeUtils.parse(dateString, GDRIVE_DATETIME_PATTERN)
}