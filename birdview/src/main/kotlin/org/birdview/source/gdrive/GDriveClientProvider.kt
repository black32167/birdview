package org.birdview.source.gdrive

import org.birdview.config.BVGDriveConfig
import org.birdview.config.BVUsersConfigProvider
import javax.inject.Named

@Named
class GDriveClientProvider(
        private val userConfigProvider: BVUsersConfigProvider,
        private val accessTokenProvider: GApiAccessTokenProvider
) {
    fun getGoogleApiClient(config: BVGDriveConfig)
            = GDriveClient(accessTokenProvider, config)
}