package org.birdview.web.form.secret

class SlackSourceSecretFormData(
        sourceName: String,
        user: String,
        val clientId: String?,
        val clientSecret: String?
): SourceSecretFormData(sourceName = sourceName, user = user, type = "slack")