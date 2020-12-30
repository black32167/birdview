package org.birdview.source.http

import org.birdview.utils.remote.ApiAuth

interface BVHttpClientFactory {
    fun getHttpClient(url:String, authProvider:() -> ApiAuth? = {null}): BVHttpClient
}