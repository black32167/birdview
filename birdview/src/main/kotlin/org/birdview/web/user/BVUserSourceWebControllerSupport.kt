package org.birdview.web.user

import org.birdview.source.SourceType
import org.birdview.storage.model.source.secrets.BVLentSecrets
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.storage.model.source.secrets.BVTokenSourceSecret
import org.birdview.web.form.secret.SourceSecretFormData
import org.birdview.web.secrets.OAuthSourceWebController

// TODO: refactor to static helpers?
abstract class BVUserSourceWebControllerSupport {
    companion object {
        const val LEND_PREFIX = "lend_from:"
    }

    protected fun getProviderAuthCodeUrl(sourceName: String, oAuthConfig: BVOAuthSourceSecret): String {
        val req = oAuthConfig.authCodeUrl +
                "client_id=${oAuthConfig.clientId}" +
                "&response_type=code" +
                "&redirect_uri=${OAuthSourceWebController.getRedirectCodeUrl(sourceName)}" +
                "&scope=${oAuthConfig.scope}" +
                "&access_type=offline"
        return req
    }

    protected fun toPersistent(sourceType: SourceType, formData: SourceSecretFormData, fallbackPrincipal: String): BVSourceSecret {
        val secret = formData.secretToken
        if (secret.startsWith(LEND_PREFIX)) {
            val (lender, sourceName) = secret.substring(LEND_PREFIX.length).split(":".toRegex(), 2)
            return BVLentSecrets(lender, sourceName)
        }

        return when (sourceType) {
            SourceType.JIRA, SourceType.CONFLUENCE, SourceType.TRELLO, SourceType.GITHUB -> BVTokenSourceSecret(
                user = formData.principal ?: fallbackPrincipal,
                token = formData.secretToken
            )
            SourceType.GDRIVE -> BVOAuthSourceSecret(
                flavor = BVOAuthSourceSecret.OAuthFlavour.GDRIVE,
                clientId = formData.principal ?: fallbackPrincipal,
                clientSecret = formData.secretToken,
                authCodeUrl = "https://accounts.google.com/o/oauth2/v2/auth?",
                tokenExchangeUrl = "https://oauth2.googleapis.com/token",
                scope = "https://www.googleapis.com/auth/drive"
            )
            SourceType.SLACK -> BVOAuthSourceSecret(
                flavor = BVOAuthSourceSecret.OAuthFlavour.SLACK,
                clientId = formData.principal ?: fallbackPrincipal,
                clientSecret = formData.secretToken,
                authCodeUrl = "https://slack.com/oauth/v2/authorize?user_scope=identity.basic&",
                tokenExchangeUrl = "https://slack.com/api/oauth.v2.access",
                scope = "channels:history,channels:read"
            )
            else -> throw UnsupportedOperationException("Unsupported secret form class: ${formData.javaClass}")
        }
    }
}