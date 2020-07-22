package org.birdview.utils.remote

import javax.ws.rs.core.Response

object ResponseValidationUtils {
    fun validate(resp: Response) {
        if (resp.status != 200) {
            throw java.lang.RuntimeException("Status:${resp.status}, message=${resp.readEntity(String::class.java)}")
        }
    }
}