package org.birdview.source.gdrive

import org.birdview.config.BVGDriveConfig
import org.birdview.config.BVUsersConfigProvider
import org.birdview.request.TasksRequest
import org.birdview.source.gdrive.model.GDriveActivityRequest
import org.birdview.source.gdrive.model.GDriveActivityResponse
import org.birdview.source.gdrive.model.GDriveFileListResponse
import org.birdview.utils.remote.BearerAuth
import org.birdview.utils.remote.WebTargetFactory
import java.time.format.DateTimeFormatter
import javax.ws.rs.client.Entity

class GDriveClient(
        private val accessTokenProvider: GApiAccessTokenProvider,
        private val userConfigProvider: BVUsersConfigProvider,
        private val config: BVGDriveConfig
) {
    companion object {
        private val SCOPE_DRIVE = "https://www.googleapis.com/auth/drive"
    }
    private val targetFactoryV2 = WebTargetFactory("https://driveactivity.googleapis.com/v2") { authCodeProvider(SCOPE_DRIVE) }
    private val targetFactoryV3 = WebTargetFactory("https://www.googleapis.com/drive/v3") { authCodeProvider(SCOPE_DRIVE) }

    fun getActivity(taskRequest:TasksRequest): GDriveActivityResponse =
        targetFactoryV2
                .getTarget("/activity:query")
                .request()
                .post(Entity.json(GDriveActivityRequest.from(taskRequest)))
                .also { response ->
                    if(response.status != 200) {
                        throw RuntimeException("Error reading Github issues: ${response.readEntity(String::class.java)}")
                    }
                }
                .readEntity(GDriveActivityResponse::class.java)

    fun getFiles(filesFilter: GDriveFilesFilter, sourceName: String): GDriveFileListResponse =
            targetFactoryV3
                    .getTarget("/files")
                    .queryParam("includeItemsFromAllDrives", true)
                    .queryParam("supportsAllDrives", true)
                    .queryParam("orderBy", "modifiedTime")
                    .queryParam("fields", "files(id,name,modifiedTime,webViewLink)")
                    .queryParam("q", getFileListQuery(filesFilter, sourceName))
                    .request()
                    .get()
                    .also { response ->
       //                 println(response.readEntity(String::class.java))
                        if(response.status != 200) {
                            throw RuntimeException("Error reading GDrive files: ${response.readEntity(String::class.java)}")
                        }
                    }
                    .readEntity(GDriveFileListResponse::class.java)

    private fun authCodeProvider(scope:String) =
            accessTokenProvider.getToken(config, scope)
            ?.let(::BearerAuth)
            ?: throw RuntimeException("Failed retrieving Google API access token")

    private fun getFileListQuery(filter: GDriveFilesFilter, sourceName: String): String =
            "'${filter.user?.let { getUser(it, sourceName) } ?: "me"}' in owners " +
                    "AND mimeType='application/vnd.google-apps.document' " +
                    "AND modifiedTime>'${filter.since.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))}'" //TODO: use taskRequest

    private fun getUser(userAlias: String, sourceName: String): String =
            userConfigProvider.getUserName(userAlias, sourceName)
}