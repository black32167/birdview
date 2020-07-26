package org.birdview.source.gdrive

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.config.BVGDriveConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.model.BVDocumentStatus
import org.birdview.model.TimeIntervalFilter
import org.birdview.source.BVTaskSource
import org.birdview.source.gdrive.model.GDriveFile
import org.birdview.utils.BVFilters
import java.util.*
import javax.inject.Named

@Named
open class GDriveTaskService(
        private val clientProvider: GDriveClientProvider,
        private val bvConfigProvider: BVSourcesConfigProvider,
        private val gDriveQueryBuilder: GDriveQueryBuilder
) : BVTaskSource {
    companion object {
        val GDRIVE_FILE_TYPE = "gDriveFile"
    }
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    override fun getTasks(user: String?, updatedPeriod: TimeIntervalFilter, chunkConsumer: (List<BVDocument>) -> Unit) {
        bvConfigProvider.getConfigOfType(BVGDriveConfig::class.java)
                ?.also { config ->
                    clientProvider.getGoogleApiClient(config)
                            .getFiles(gDriveQueryBuilder.getQuery(user, updatedPeriod, config.sourceName)) { files->
                                chunkConsumer.invoke(files.map { file -> toBVDocument(file, config) })
                            }
                }
    }

    override fun getType() = "gdrive"

    private fun toBVDocument(file: GDriveFile, config: BVGDriveConfig) =
            BVDocument(
                        ids = setOf(BVDocumentId(id = file.id, type = GDRIVE_FILE_TYPE, sourceName = config.sourceName)),
                        refsIds = BVFilters.filterIdsFromText(file.name),
                        title = file.name,
                        updated = parseDate(file.modifiedTime),
                        httpUrl = file.webViewLink,
                        status = BVDocumentStatus.PROGRESS,
                        key = "open"
            )

    private fun parseDate(dateString: String): Date = dateTimeFormat.parse(dateString)
}