package org.birdview.source.gdrive

import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.gdrive.model.GDriveFile
import org.birdview.source.gdrive.model.GDriveFileListResponse
import org.birdview.source.http.BVHttpSourceClientFactory
import org.birdview.utils.BVTimeUtil
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class GDriveClient(
    private val httpClientFactory: BVHttpSourceClientFactory
) {
    private val log = LoggerFactory.getLogger(GDriveClient::class.java)
    private val filesPerPage = 500

    fun getFiles(config: BVSourceConfigProvider.SyntheticSourceConfig, query: String?, chunkConsumer: (List<GDriveFile>) -> Unit) {
        if (query == null) {
            return
        } else {
            log.info("Running GDrive query '{}'", query)

            val mainParameters:Map<String, Any> = mapOf(
                "includeItemsFromAllDrives" to true,
                "supportsAllDrives" to true,
                "pageSize" to filesPerPage,
                "orderBy" to "modifiedTime desc",
                "fields" to "files(id,name,modifiedTime,webViewLink,owners,modifiedByMe,modifiedByMeTime,lastModifyingUser,sharingUser),nextPageToken",
            )
            var qParameters:Map<String, Any> = mainParameters + mapOf("q" to query)
            do {
                val filesResponse = BVTimeUtil.logTimeAndReturn("gdrive-getFiles-page") {
                    getHttpClient(config)
                        .get(
                            resultClass = GDriveFileListResponse::class.java,
                            subPath="/files",
                            parameters = qParameters)
                }
                .also {
                    log.info("Loaded {} GDrive docs for query ${query}", it.files.size)
                    chunkConsumer.invoke(it.files)
                }
                .takeIf { it.nextPageToken != null }
                ?.also {
                    qParameters = mainParameters + (it.nextPageToken?.let { nextPageToken->
                        mapOf("pageToken" to nextPageToken)
                    } ?: emptyMap())
                }

            } while (filesResponse != null)
        }
    }

    private fun getHttpClient(config: BVSourceConfigProvider.SyntheticSourceConfig) =
        httpClientFactory.createClient(config)
}