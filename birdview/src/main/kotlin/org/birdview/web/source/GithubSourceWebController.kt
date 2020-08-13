package org.birdview.web.source

import org.birdview.config.BVGithubConfig
import org.birdview.config.BVSourcesConfigProvider
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/settings/github")
class GithubSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider
): AbstractSourceWebController<BVGithubConfig>(sourcesConfigProvider) {
    class GithubSourceFormData(
            val sourceName:String,
            val key: String?,
            val secret: String?,
            val baseUrl: String?
    )

    @PostMapping("/add-source")
    fun addSource(model: Model, @ModelAttribute sourceFormData: GithubSourceFormData): ModelAndView {
        saveConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    @PostMapping("/update-source")
    fun updateSource(model: Model, @ModelAttribute sourceFormData: GithubSourceFormData): ModelAndView {
        updateConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    override fun getConfigClass() = BVGithubConfig::class.java

    private fun mapConfig(sourceFormData: GithubSourceFormData) =
            BVGithubConfig (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.key!!,
                    token = sourceFormData.secret!!)
}