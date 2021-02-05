package org.birdview.source.gdrive

import org.birdview.source.gdrive.model.GDriveFile
import org.birdview.source.gdrive.model.GDriveFileListResponse
import org.birdview.source.http.BVHttpClientFactory
import org.birdview.source.oauth.AbstractOAuthClient
import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.storage.BVGDriveConfig
import org.birdview.storage.BVOAuthSourceConfig
import org.birdview.utils.BVTimeUtil
import org.birdview.utils.remote.BearerAuth
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named
class GDriveClient(
    private val httpClientFactory: BVHttpClientFactory,
    tokenStorage: OAuthRefreshTokenStorage
): AbstractOAuthClient<GAccessTokenResponse>(tokenStorage, httpClientFactory) {
    private val log = LoggerFactory.getLogger(GDriveClient::class.java)
    private val filesPerPage = 500

    fun getFiles(config: BVGDriveConfig, query: String?, chunkConsumer: (List<GDriveFile>) -> Unit) {
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
                val filesResponse = BVTimeUtil.logTime("gdrive-getFiles-page") {
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

    private fun authCodeProvider(config: BVGDriveConfig) =
            getToken(config)
            ?.let(::BearerAuth)
            ?: throw RuntimeException("Failed retrieving Google API access token")

    override fun getTokenRefreshFormContent(refreshToken:String, config: BVOAuthSourceConfig): Map<String, String> =
            mapOf(
                    "client_id" to config.clientId,
                    "client_secret" to config.clientSecret,
                    "grant_type" to "refresh_token",
                    "refresh_token" to refreshToken)

    override fun readAccessTokenResponse(response: GAccessTokenResponse): String = response.access_token

    private fun getHttpClient(config: BVGDriveConfig) =
        httpClientFactory.getHttpClient("https://www.googleapis.com/drive/v3") {
            authCodeProvider(config)
        }

    override fun getAccessTokenResponseClass(): Class<GAccessTokenResponse> = GAccessTokenResponse::class.java
}