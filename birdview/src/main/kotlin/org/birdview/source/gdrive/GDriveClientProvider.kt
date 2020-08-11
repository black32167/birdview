package org.birdview.source.gdrive

import org.birdview.config.BVGDriveConfig
import org.birdview.web.BVOAuthController
import javax.inject.Named

@Named
class GDriveClientProvider(
        private val oauthController: BVOAuthController
) {
    fun getGoogleApiClient(config: BVGDriveConfig)
            = GDriveClient(oauthController, config)

    fun isAuthenticated(config: BVGDriveConfig)
            = oauthController.hasToken(config)
}