package org.birdview.web.form.secret

class SlackSourceSecretFormData(
        val email: String,
        val clientId: String,
        val clientSecret: String
): SourceSecretFormData()