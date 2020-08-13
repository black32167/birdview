package org.birdview.web.source

import org.birdview.config.BVSourcesConfigProvider
import org.birdview.config.BVTrelloConfig
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/settings/trello")
class TrelloSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider
): AbstractSourceWebController<BVTrelloConfig>(sourcesConfigProvider) {
    class TrelloSourceFormData(
            val sourceName: String,
            val key: String?,
            val secret: String?,
            val baseUrl: String?
    )

    @PostMapping("/add-source")
    fun addSource(model: Model, @ModelAttribute sourceFormData: TrelloSourceFormData): ModelAndView {
        saveConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    @PostMapping("/update-source")
    fun updateSource(model: Model, @ModelAttribute sourceFormData: TrelloSourceFormData): ModelAndView {
        updateConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    override fun getConfigClass() = BVTrelloConfig::class.java

    private fun mapConfig(sourceFormData: TrelloSourceFormData) =
            BVTrelloConfig(
                    sourceName = sourceFormData.sourceName,
                    key = sourceFormData.sourceName,
                    token = sourceFormData.secret!!)
}
