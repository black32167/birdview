package org.birdview.source.http

import org.birdview.utils.remote.ApiAuth
import javax.inject.Named

@Named
class BVDefaultHttpClientFactory: BVHttpClientFactory {
    override fun getHttpClient(url: String, authProvider: () -> ApiAuth?): BVHttpClient =
        BVHttpClientImpl(basePath = url, authProvider = authProvider)
}
