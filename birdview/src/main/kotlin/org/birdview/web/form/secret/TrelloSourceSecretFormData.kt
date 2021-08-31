package org.birdview.web.form.secret

class TrelloSourceSecretFormData(
        val email:String,
        val key: String,
        val secret: String
): SourceSecretFormData() {
        override fun getSecretToken() = secret
}
