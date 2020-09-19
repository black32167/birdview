package org.birdview.web

import org.birdview.config.BVSourcesConfigProvider
import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Controller
@RequestMapping(BVWebPaths.SETTINGS)
class BVSettingsController(
        private val sourcesConfigProvider: BVSourcesConfigProvider,
        private val sources: List<BVTaskSource>
) {
    class SourceSettingView(
            val name: String,
            val type: SourceType)

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

        return "source/add-source"
    }

    @GetMapping("/delete")
    fun deleteSource(model: Model, @RequestParam("sourceName") sourceName: String): ModelAndView {
        sourcesConfigProvider.delete(sourceName)

        return ModelAndView("redirect:${BVWebPaths.SETTINGS}")
    }

    private fun mapSourceSetting(sourceName: String): SourceSettingView? =
            sourcesConfigProvider.getConfigByName(sourceName)
                    ?.let { sourceConfig ->
                        SourceSettingView(
                                name = sourceConfig.sourceName,
                                type = sourceConfig.sourceType
                        )
                    }

    private fun getBaseUrl() =
            ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
}