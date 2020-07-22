package org.birdview.source.gdrive

import org.birdview.config.BVGDriveConfig
import org.birdview.source.ItemsIterable
import org.birdview.source.ItemsPage
import org.birdview.source.gdrive.model.GDriveFile
import org.birdview.source.gdrive.model.GDriveFileListResponse
import org.birdview.utils.remote.BearerAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response

class GDriveClient(
        private val accessTokenProvider: GApiAccessTokenProvider,
        private val config: BVGDriveConfig
) {
    companion object {
        private val SCOPE_DRIVE = "https://www.googleapis.com/auth/drive"
    }

    private val log = LoggerFactory.getLogger(GDriveClient::class.java)
    private val filesPerPage = 50
    private val targetFactoryV3 = WebTargetFactory("https://www.googleapis.com/drive/v3") { authCodeProvider(SCOPE_DRIVE) }

    fun getFiles(query: String?): Iterable<GDriveFile> =
            if (query == null) {
                emptyList()
            } else {
                log.info("Running GDrive query '{}'", query)
                filesTarget(query)
                        .request()
                        .get()
                        .let { resp ->
                            ItemsIterable(mapFilesPage (resp)) { nextPageToken ->
                                log.info("Loading gDrive next page: {}", nextPageToken)
                                filesTarget(query)
                                        .queryParam("pageToken", nextPageToken)
                                        ?.request()
                                        ?.get()
                                        ?.let (this::mapFilesPage)
                            }
                        }
            }

    private fun filesTarget(query: String) = targetFactoryV3
            .getTarget("/files")
            .queryParam("includeItemsFromAllDrives", true)
            .queryParam("supportsAllDrives", true)
            .queryParam("pageSize", filesPerPage)
            .queryParam("orderBy", "modifiedTime desc")
            .queryParam("fields", "files(id,name,modifiedTime,webViewLink),nextPageToken")
            .queryParam("q", query)

    private fun mapFilesPage(response: Response): ItemsPage<GDriveFile, String> =
            response
                    .also (ResponseValidationUtils::validate)
                    .let { resp ->
                        val filesResponse = resp.readEntity(GDriveFileListResponse::class.java)
                        ItemsPage (
                                filesResponse.files,
                                filesResponse.nextPageToken)
                    }
    private fun authCodeProvider(scope:String) =
            accessTokenProvider.getToken(config, scope)
            ?.let(::BearerAuth)
            ?: throw RuntimeException("Failed retrieving Google API access token")

}