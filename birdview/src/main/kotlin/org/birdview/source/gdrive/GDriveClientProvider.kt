package org.birdview.source.gdrive

import org.birdview.source.oauth.OAuthRefreshTokenStorage
import org.birdview.storage.BVGDriveConfig
import javax.inject.Named

@Named
class GDriveClientProvider(
        private val tokenStorage: OAuthRefreshTokenStorage
) {
    fun getGoogleApiClient(config: BVGDriveConfig)
            = GDriveClient(config, tokenStorage)

    fun isAuthenticated(config: BVGDriveConfig)
            = tokenStorage.hasToken(config)
}