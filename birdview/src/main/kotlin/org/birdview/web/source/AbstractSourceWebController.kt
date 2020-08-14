package org.birdview.web.source

import org.birdview.config.BVAbstractSourceConfig
import org.birdview.config.BVSourcesConfigProvider
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

abstract class AbstractSourceWebController<T : BVAbstractSourceConfig, F> (
        private val sourcesConfigProvider: BVSourcesConfigProvider
) {

    @GetMapping("/edit-source")
    fun editSourceView1(model: Model, @RequestParam("sourceName") sourceName: String): String {
        val config = getConfig(sourceName)!!
        model.addAttribute("source", mapForm(config))

        return "source/edit-source-${config.sourceType.name.toLowerCase()}"
    }

    @PostMapping("/add-source")
    fun addSource1(model: Model, @ModelAttribute sourceFormData: F): ModelAndView {
        saveConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }

    @PostMapping("/update-source")
    fun updateSource1(model: Model, @ModelAttribute sourceFormData: F): ModelAndView {
        updateConfig(mapConfig(sourceFormData))
        return ModelAndView("redirect:/settings");
    }


    protected fun saveConfig(config:T) {
        sourcesConfigProvider.save(config)
    }

    protected fun updateConfig(config:T) {
        sourcesConfigProvider.update(config)
    }

    protected fun  getConfig(sourceName: String): T? =
            sourcesConfigProvider.getConfigByName(sourceName, getConfigClass())

    protected abstract fun mapConfig(sourceFormData: F): T
    protected abstract fun mapForm(config: T): F
    protected abstract fun getConfigClass(): Class<T>
}