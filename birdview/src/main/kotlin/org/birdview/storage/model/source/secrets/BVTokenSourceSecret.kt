package org.birdview.storage.model.source.secrets

open class BVTokenSourceSecret (
    val user: String,
    val token: String
) : BVSourceSecret
