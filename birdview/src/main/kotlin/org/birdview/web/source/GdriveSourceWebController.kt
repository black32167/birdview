package org.birdview.web.source

import org.birdview.config.BVGDriveConfig
import org.birdview.config.BVSourcesConfigProvider
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/settings/gdrive")
class GdriveSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider
): AbstractSourceWebController<BVGDriveConfig>(sourcesConfigProvider) {
    class GdriveSourceFormData(
            val sourceName: String,
            val key: String?,
            val secret: String?,
            val baseUrl: String?
    )

    @PostMapping("/add-source")
    fun addSource(model: Model, @ModelAttribute sourceFormData: GdriveSourceFormData): ModelAndView {
        saveConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    @PostMapping("/update-source")
    fun updateSource(model: Model, @ModelAttribute sourceFormData: GdriveSourceFormData): ModelAndView {
        updateConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    override fun getConfigClass() = BVGDriveConfig::class.java

    private fun mapConfig(sourceFormData: GdriveSourceFormData) =
            BVGDriveConfig (
                    sourceName = sourceFormData.sourceName,
                    clientId = sourceFormData.key!!,
                    clientSecret = sourceFormData.secret!!)
}
