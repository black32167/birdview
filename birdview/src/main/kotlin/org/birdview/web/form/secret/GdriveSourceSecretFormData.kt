package org.birdview.web.form.secret

class GdriveSourceSecretFormData(
        sourceName: String,
        user: String,
        val key: String?,
        val secret: String?
): SourceSecretFormData(sourceName = sourceName, user = user, type = "gdrive")