package org.birdview.source.http

import org.birdview.source.gdrive.GDriveOAuthClient
import org.birdview.source.slack.SlackOAuthClient
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.storage.model.source.secrets.BVTokenSourceSecret
import org.birdview.utils.remote.ApiAuth
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.BearerAuth
import javax.inject.Named

@Named
class BVHttpSourceClientFactory(
    val httpClientFactory: BVHttpClientFactory,
    val gdriveOAuthClient: GDriveOAuthClient,
    val slackOAuthClient: SlackOAuthClient,
) {
    fun createClient(sourceName:String, sourceSecret: BVSourceSecret, baseApiUrl: String): BVHttpClient =
        httpClientFactory.getHttpClient(baseApiUrl) {
            getAuth(sourceName, sourceSecret)
        }

    private fun getAuth(sourceName:String, sourceSecret: BVSourceSecret): ApiAuth =
        when (sourceSecret) {
            is BVTokenSourceSecret -> BasicAuth(sourceSecret.user, sourceSecret.token)
            is BVOAuthSourceSecret -> when (sourceSecret.flavor) {
                BVOAuthSourceSecret.OAuthFlavour.GDRIVE -> gdriveOAuthClient.getToken(sourceName, sourceSecret)
                    ?.let(::BearerAuth)
                    ?: throw RuntimeException("Failed retrieving Google API access token")
                BVOAuthSourceSecret.OAuthFlavour.SLACK -> slackOAuthClient.getToken(sourceName, sourceSecret)
                    ?.let(::BearerAuth)
                    ?: throw RuntimeException("Failed retrieving Google API access token")
            }
            else -> throw RuntimeException("Unsupported auth type: ${sourceSecret}")
        }
}