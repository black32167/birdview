package org.birdview.source.gdrive

import org.birdview.config.BVGDriveConfig
import org.birdview.source.gdrive.model.GDriveFileListResponse
import org.birdview.utils.remote.BearerAuth
import org.birdview.utils.remote.WebTargetFactory

class GDriveClient(
        private val accessTokenProvider: GApiAccessTokenProvider,
        private val config: BVGDriveConfig
) {
    companion object {
        private val SCOPE_DRIVE = "https://www.googleapis.com/auth/drive"
    }
    private val targetFactoryV2 = WebTargetFactory("https://driveactivity.googleapis.com/v2") { authCodeProvider(SCOPE_DRIVE) }
    private val targetFactoryV3 = WebTargetFactory("https://www.googleapis.com/drive/v3") { authCodeProvider(SCOPE_DRIVE) }

    fun getFiles(query: String?): GDriveFileListResponse? =
            if (query == null) {
                null
            } else {
                targetFactoryV3
                        .getTarget("/files")
                        .queryParam("includeItemsFromAllDrives", true)
                        .queryParam("supportsAllDrives", true)
                        .queryParam("orderBy", "modifiedTime")
                        .queryParam("fields", "files(id,name,modifiedTime,webViewLink)")
                        .queryParam("q", query)
                        .request()
                        .get()
                        .also { response ->
                            //                 println(response.readEntity(String::class.java))
                            if (response.status != 200) {
                                throw RuntimeException("Error reading GDrive files: ${response.readEntity(String::class.java)}")
                            }
                        }
                        .readEntity(GDriveFileListResponse::class.java)
            }

    private fun authCodeProvider(scope:String) =
            accessTokenProvider.getToken(config, scope)
            ?.let(::BearerAuth)
            ?: throw RuntimeException("Failed retrieving Google API access token")

}