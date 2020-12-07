package org.birdview.web.explore.model

import java.util.*

class BVUserLogEntry (
    var timestamp: String,
    var message: String) {
    val id = UUID.randomUUID().toString()
}