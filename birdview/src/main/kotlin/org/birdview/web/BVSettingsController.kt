package org.birdview.web

import org.birdview.config.*
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Controller
@RequestMapping("/settings")
class BVSettingsController(
        private val oauthController: BVOAuthController,
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        private val sources: List<BVTaskSource>
) {
    class SourceSettingView(
            val name: String,
            val authenticated: Boolean,
            val type: SourceType,
            val authenticationLink: String = "",
            val authUrl: String? = null)

    class SourceFormData(
            val sourceName:String,
            val sourceType: SourceType,
            val key: String?,
            val secret: String?,
            val baseUrl: String?
    )

    private val sourcesTypesMap = sources.associateBy { it.getType() }

    @GetMapping
    fun index(model: Model): String? {
        model
                .addAttribute("sources", sourcesConfigProvider.listSourceNames().map { mapSourceSetting(it) })
        return "settings"
    }

    @GetMapping("/add-source")
    fun addSource(model: Model): String {
        model.addAttribute("sourceTypes", sources.map { it.getType().name.toLowerCase() })

        return "add-source"
    }

    @GetMapping("/delete")
    fun deleteSource(model: Model, @RequestParam("sourceName") sourceName: String): ModelAndView {
        sourcesConfigProvider.delete(sourceName)

        return ModelAndView("redirect:/settings");
    }


    @PostMapping("/add-source-post")
    fun postSource(model: Model, @ModelAttribute form:SourceFormData): ModelAndView {
        sourcesConfigProvider.save(mapSource(form))
        return ModelAndView("redirect:/settings");
    }

    private fun mapSource(sourceFormData: SourceFormData): BVAbstractSourceConfig =
            when(sourceFormData.sourceType) {
                SourceType.JIRA -> BVJiraConfig (
                        sourceName = sourceFormData.sourceName,
                        user = sourceFormData.key!!,
                        token = sourceFormData.secret!!,
                        baseUrl = sourceFormData.baseUrl!!
                )
                SourceType.TRELLO -> BVTrelloConfig (
                        sourceName = sourceFormData.sourceName,
                        key = sourceFormData.sourceName,
                        token = sourceFormData.secret!!)
                SourceType.GITHUB -> BVGithubConfig (
                        sourceName = sourceFormData.sourceName,
                        user= sourceFormData.key!!,
                        token = sourceFormData.secret!!)
                SourceType.GDRIVE -> BVGDriveConfig (
                        sourceName = sourceFormData.sourceName,
                        clientId = sourceFormData.key!!,
                        clientSecret = sourceFormData.secret!!
                )
            }

    private fun mapSourceSetting(sourceName: String): SourceSettingView? =
            sourcesConfigProvider.getConfigByName(sourceName)
                    ?.let { sourceConfig -> sourcesTypesMap[sourceConfig.sourceType]
                            ?.let { sourceHandler ->
                                SourceSettingView(
                                        name = sourceConfig.sourceName,
                                        authenticated = sourceHandler.isAuthenticated(sourceConfig.sourceName),
                                        type = sourceConfig.sourceType,
                                        authUrl = (sourceConfig as? BVOAuthSourceConfig)?.let(oauthController::getAuthTokenUrl)
                                )
                            } }


    private fun getBaseUrl() =
            ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
}