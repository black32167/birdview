package org.birdview.web.source

import org.birdview.config.BVJiraConfig
import org.birdview.config.BVSourcesConfigProvider
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/settings/jira")
class JiraSourceWebController(
        sourcesConfigProvider: BVSourcesConfigProvider
): AbstractSourceWebController<BVJiraConfig>(sourcesConfigProvider) {
    class JiraSourceFormData(
            val sourceName:String,
            val key: String?,
            val secret: String?,
            val baseUrl: String?
    )


    @GetMapping("/edit-source")
    fun editSourceView(model: Model, @RequestParam("sourceName") sourceName: String): String {
        val config = getConfig(sourceName)!!
        model.addAttribute("source", JiraSourceFormData(
                sourceName = config.sourceName,
                key = config.user,
                baseUrl = config.baseUrl,
                secret = config.token
        ))

        return "source/edit-source-jira"
    }

    @PostMapping("/add-source")
    fun addSource(model: Model, @ModelAttribute sourceFormData: JiraSourceFormData): ModelAndView {
        saveConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    @PostMapping("/update-source")
    fun updateSource(model: Model, @ModelAttribute sourceFormData: JiraSourceFormData): ModelAndView {
        updateConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    override fun getConfigClass() = BVJiraConfig::class.java

    private fun mapConfig(sourceFormData: JiraSourceFormData) =
            BVJiraConfig (
                    sourceName = sourceFormData.sourceName,
                    user = sourceFormData.key!!,
                    token = sourceFormData.secret!!,
                    baseUrl = sourceFormData.baseUrl!!)
}