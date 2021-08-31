package org.birdview.web.form.secret

class GithubSourceSecretFormData(
        val user: String,
        val secret: String
): SourceSecretFormData() {
        override fun getSecretToken() = secret
}
