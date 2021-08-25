package org.birdview.web.form

import org.birdview.source.SourceType
import org.birdview.web.form.secret.SourceSecretFormData

class CreateUserSourceFormData (
        val sourceName: String,
        val baseUrl: String,
        val sourceType: SourceType,
        val sourceSecretFormData: SourceSecretFormData
)