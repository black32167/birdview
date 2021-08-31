package org.birdview.web.form.secret

class ConfluenceSourceSecretFormData(
        val email: String,
        val secret: String,
        val user: String
): SourceSecretFormData() {
        override fun getSecretToken() = secret
}
