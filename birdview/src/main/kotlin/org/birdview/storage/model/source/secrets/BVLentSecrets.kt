package org.birdview.storage.model.source.secrets

data class BVLentSecrets (
    val lenderUser: String,
    val lenderSourceName: String
) : BVSourceSecret