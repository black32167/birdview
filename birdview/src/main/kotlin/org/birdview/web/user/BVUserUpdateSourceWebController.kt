package org.birdview.web.user

import org.birdview.security.UserContext
import org.birdview.storage.BVUserSourceConfigStorage
import org.birdview.storage.SourceSecretsMapper
import org.birdview.storage.model.source.config.BVUserSourceConfig
import org.birdview.storage.model.source.secrets.BVLentSecrets
import org.birdview.storage.model.source.secrets.BVOAuthSourceSecret
import org.birdview.storage.model.source.secrets.BVSourceSecret
import org.birdview.storage.model.source.secrets.BVTokenSourceSecret
import org.birdview.web.BVWebPaths
import org.birdview.web.form.UpdateUserSourceFormData
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import javax.ws.rs.NotFoundException

@Controller
@RequestMapping("/user/source/{sourceName}")
class BVUserUpdateSourceWebController(
    private val userSourceStorage: BVUserSourceConfigStorage,
    private val sourceSecretsMapper: SourceSecretsMapper
): BVUserSourceWebControllerSupport() {

    @GetMapping
    fun editForm(model: Model, @PathVariable("sourceName") sourceName:String): String {
        val sourceConfig = userSourceStorage.getSource(bvUser = UserContext.getUserName(), sourceName = sourceName)
            ?: throw NotFoundException("Unknown source: '${sourceName}'")
        val secret:BVSourceSecret = sourceSecretsMapper.deserialize(sourceConfig.serializedSourceSecret)
        model
            .addAttribute("sourceName", sourceName)
            .addAttribute("baseUrl", sourceConfig.baseUrl)
            .addAttribute("sourceType", sourceConfig.sourceType.alias)
            .addAttribute("sourceUserName", sourceConfig.sourceUserName)
            .addAttribute("enabled", if (sourceConfig.enabled) UpdateUserSourceFormData.YES else UpdateUserSourceFormData.NO)
            .addAttribute("filter", sourceConfig.sourceUserName)
            .addAttribute("principal", resolvePrincipal(secret))
            .addAttribute("secretToken", resolveSecretToken(secret))
        if(secret is BVOAuthSourceSecret) {
            model.addAttribute("authorizationUrl", getProviderAuthCodeUrl(sourceName, secret))
        }
        return "/user/edit-source"
    }


    @PostMapping
    fun update(@PathVariable("sourceName") sourceName:String,
               formDataUpdate: UpdateUserSourceFormData
    ): Any {
        val bvUser = UserContext.getUserName()
//        val sourceManager = sourcesProvider.getBySourceName(
//            bvUser = bvUser, sourceName = sourceName)
//
//        if(sourceManager != null) {
//            val resolved = sourceManager.resolveSourceUserId(bvUser, sourceName, formDataUpdate.sourceUserName)
//            println(resolved)
//        }

        userSourceStorage.update(
            bvUser = bvUser,
            sourceConfig = BVUserSourceConfig(
                sourceName = sourceName,
                sourceUserName = formDataUpdate.filter,
                enabled = formDataUpdate.enabled != null,
                baseUrl = formDataUpdate.baseUrl,
                sourceType = formDataUpdate.sourceType,
                serializedSourceSecret = sourceSecretsMapper.serialize(
                    toPersistent(formDataUpdate.sourceType, formDataUpdate.sourceSecretFormData, formDataUpdate.filter))
            )
        )
        return "redirect:${BVWebPaths.USER_SETTINGS}"
    }

    private fun resolvePrincipal(secretConfig: BVSourceSecret): String? =
        when (secretConfig) {
            is BVTokenSourceSecret -> secretConfig.user
            is BVOAuthSourceSecret -> secretConfig.clientId
            is BVLentSecrets -> secretConfig.lenderUser
            else -> null
        }


    private fun resolveSecretToken(secretConfig: BVSourceSecret): String? =
        when (secretConfig) {
            is BVTokenSourceSecret -> secretConfig.token
            is BVOAuthSourceSecret -> secretConfig.clientSecret
            is BVLentSecrets -> "${LEND_PREFIX}${secretConfig.lenderUser}:${secretConfig.lenderSourceName}"
            else -> null
        }
}