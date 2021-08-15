package org.birdview.web.form.secret

class GithubSourceSecretFormData(
        sourceName:String,
        user: String,
        val secret: String?
): SourceSecretFormData(sourceName = sourceName, user = user, type = "github")