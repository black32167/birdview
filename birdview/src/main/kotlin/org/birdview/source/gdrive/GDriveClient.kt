package org.birdview.source.gdrive

import org.birdview.config.sources.BVGDriveConfig
import org.birdview.config.sources.BVOAuthSourceConfig
import org.birdview.source.ItemsPage
import org.birdview.source.gdrive.model.GDriveFile
import org.birdview.source.gdrive.model.GDriveFileListResponse
import org.birdview.source.oauth.AbstractOAuthClient
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.utils.BVTimeUtil
import org.birdview.utils.remote.BearerAuth
import org.birdview.utils.remote.ResponseValidationUtils
import org.birdview.utils.remote.WebTargetFactory
import org.slf4j.LoggerFactory
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Form
import javax.ws.rs.core.Response

class GDriveClient(
        private val config: BVGDriveConfig,
        tokenStorage: OAuthRefreshTokenStorage
): AbstractOAuthClient(tokenStorage) {
    private val log = LoggerFactory.getLogger(GDriveClient::class.java)
    private val filesPerPage = 500

    private val targetFactoryV3 =
            WebTargetFactory("https://www.googleapis.com/drive/v3") {
                authCodeProvider()
            }

    fun getFiles(query: String?, chunkConsumer: (List<GDriveFile>) -> Unit) {
        if (query == null) {
            return
        } else {
            log.info("Running GDrive query '{}'", query)

            var target:WebTarget? = filesTarget(query)
            do {
                val page = BVTimeUtil.logTime("gdrive-getFiles-page") {
                    target
                            ?.request()
                            ?.get()
                            ?.let(this::mapFilesPage)
                            ?.takeUnless { it.items.isEmpty() }
                }

                page?.also {  chunkConsumer.invoke(it.items) }
                target = page?.continuation
                        ?.let { nextPageToken->
                            filesTarget(query).queryParam("pageToken", nextPageToken)
                        }

            } while (target != null)
        }
    }

    private fun filesTarget(query: String) = targetFactoryV3
            .getTarget("/files")
            .queryParam("includeItemsFromAllDrives", true)
            .queryParam("supportsAllDrives", true)
            .queryParam("pageSize", filesPerPage)
            .queryParam("orderBy", "modifiedTime desc")
            .queryParam("fields", "files(id,name,modifiedTime,webViewLink,owners,modifiedByMe,modifiedByMeTime,lastModifyingUser,sharingUser),nextPageToken")
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

    private fun authCodeProvider() =
            getToken(config)
            ?.let(::BearerAuth)
            ?: throw RuntimeException("Failed retrieving Google API access token")

    override fun getTokenRefreshFormEntity(refreshToken:String, config: BVOAuthSourceConfig): Entity<Form> =
            Entity.form(Form()
                    .param("client_id", config.clientId)
                    .param("client_secret", config.clientSecret)
                    .param("grant_type", "refresh_token")
                    .param("refresh_token", refreshToken))

    override fun readAccessTokenResponse(response: Response): String = response
                    .readEntity(GAccessTokenResponse::class.java)
                    .access_token
}