package org.birdview.web.form

import org.birdview.source.SourceType
import org.birdview.web.form.secret.SourceSecretFormData

class UpdateUserSourceFormData(
    val enabled: String?,
    val baseUrl: String,
    val sourceType: SourceType,
    val filter: String,
    val sourceSecretFormData: SourceSecretFormData
) {
    companion object {
        val YES = "yes"
        val NO = "no"
    }
}