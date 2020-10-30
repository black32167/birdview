package org.birdview.utils.remote

import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response

object ResponseValidationUtils {
    private val log = LoggerFactory.getLogger(ResponseValidationUtils::class.java)
    fun validate(resp: Response) {
        if (resp.status != 200) {
            val entityString = resp.readEntity(String::class.java)
            log.error("Status:${resp.status}, message=${entityString}")
            throw java.lang.RuntimeException("Status:${resp.status}, message=${entityString}")
        }
    }
}