package org.birdview.web.source

import org.birdview.config.sources.BVSourcesConfigStorage
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Controller
@RequestMapping(BVWebPaths.SECRETS)
class BVSourceSecretListController(
        private val sourcesConfigStorage: BVSourcesConfigStorage,
        private val sources: List<BVTaskSource>
) {
    class SourceSettingView(
            val name: String,
            val type: SourceType)

    private val sourcesTypesMap = sources.associateBy { it.getType() }

    @GetMapping
    fun index(model: Model): String? {
        model
                .addAttribute("sources", sourcesConfigStorage.listSourceNames().map { mapSourceSetting(it) })
        return "secrets/list-secrets"
    }

    @GetMapping("/add-secret")
    fun addSource(model: Model): String {
        model.addAttribute("sourceTypes", sources.map { it.getType().name.toLowerCase() })

        return "secrets/add-secret"
    }

    @GetMapping("/delete")
    fun deleteSource(model: Model, @RequestParam("sourceName") sourceName: String): ModelAndView {
        sourcesConfigStorage.delete(sourceName)

        return ModelAndView("redirect:${BVWebPaths.SECRETS}")
    }

    private fun mapSourceSetting(sourceName: String): SourceSettingView? =
            sourcesConfigStorage.getConfigByName(sourceName)
                    ?.let { sourceConfig ->
                        SourceSettingView(
                                name = sourceConfig.sourceName,
                                type = sourceConfig.sourceType
                        )
                    }

    private fun getBaseUrl() =
            ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
}