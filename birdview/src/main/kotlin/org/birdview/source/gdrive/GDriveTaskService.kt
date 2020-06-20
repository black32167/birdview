package org.birdview.source.gdrive

import org.birdview.analysis.BVDocument
import org.birdview.analysis.BVDocumentId
import org.birdview.config.BVGDriveConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.request.TasksRequest
import org.birdview.source.BVTaskSource
import org.birdview.source.gdrive.model.GDriveFile
import java.util.*
import javax.inject.Named

@Named
class GDriveTaskService(
        private val clientProvider: GDriveClientProvider,
        private val bvConfigProvider: BVSourcesConfigProvider
) : BVTaskSource {
    companion object {
        val GDRIVE_FILE_TYPE = "gDriveFile"
    }
    private val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    override fun getTasks(request: TasksRequest): List<BVDocument> =
            bvConfigProvider.getConfigOfType(BVGDriveConfig::class.java)
                    ?.let { config ->
                        clientProvider.getGoogleApiClient(config)
                                .getFiles(request, config.sourceName)
                                .files
                                .map { file -> toBVDocument(file, config) }
                    }
                    ?:emptyList()

    override fun getType() = "gdrive"

    private fun toBVDocument(file: GDriveFile, config: BVGDriveConfig) =
            BVDocument(
                        ids = setOf(BVDocumentId(id = file.id, type = GDRIVE_FILE_TYPE, sourceName = config.sourceName)),
                        title = file.name,
                        updated = parseDate(file.modifiedTime),
                        httpUrl = file.webViewLink,
                        status = "open")

    private fun parseDate(dateString: String): Date = dateTimeFormat.parse(dateString)
}