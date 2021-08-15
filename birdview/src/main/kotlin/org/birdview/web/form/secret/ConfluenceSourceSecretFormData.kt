package org.birdview.web.form.secret

class ConfluenceSourceSecretFormData(
        sourceName:String,
        user: String,
        val secret: String?,
        val baseUrl: String?
): SourceSecretFormData(sourceName = sourceName, user = user, type = "confluence")