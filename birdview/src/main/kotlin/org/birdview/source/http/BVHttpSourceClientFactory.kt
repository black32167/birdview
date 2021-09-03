package org.birdview.source.http

import org.birdview.BVCacheNames
import org.birdview.source.BVSourceConfigProvider
import org.birdview.source.gdrive.GDriveOAuthClient
import org.birdview.source.slack.SlackOAuthClient
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.storage.model.source.secrets.BVTokenSourceSecret
import org.birdview.utils.remote.ApiAuth
import org.birdview.utils.remote.BasicAuth
import org.birdview.utils.remote.BearerAuth
import org.springframework.cache.annotation.Cacheable
import javax.inject.Named

@Named
open class BVHttpSourceClientFactory(
    val httpClientFactory: BVHttpClientFactory,
    val gdriveOAuthClient: GDriveOAuthClient,
    val slackOAuthClient: SlackOAuthClient,
    val sourceConfigProvider: BVSourceConfigProvider
) {
    @Cacheable(cacheNames = arrayOf(BVCacheNames.HTTP_CLIENT_CACHE_NAME), sync = true)
    open fun createClient(bvUser: String, sourceName:String, url: String): BVHttpClient {
        return httpClientFactory.getHttpClientAuthenticated(url) {
            val sourceConfig = sourceConfigProvider.getSourceConfig(sourceName = sourceName, bvUser = bvUser)
                ?: throw IllegalArgumentException("Could not find config for source ${sourceName}, user ${bvUser}")
            getAuth(sourceName, sourceConfig.sourceSecret)
        }
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