package org.birdview.web.source

import org.birdview.config.BVAbstractSourceConfig
import org.birdview.config.BVSourcesConfigProvider
import org.birdview.web.BVWebPaths
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

abstract class AbstractSourceWebController<T : BVAbstractSourceConfig, F> (
        private val sourcesConfigProvider: BVSourcesConfigProvider
) {
    abstract class AbstractSourceFormData(
            val sourceName:String
    )

    @GetMapping("/edit-source")
    fun editSourceView1(model: Model, @RequestParam("sourceName") sourceName: String): String {
        val config = getConfig(sourceName)!!
        model.addAttribute("source", mapForm(config))

        return "source/edit-source"
    }

    @PostMapping("/add-source")
    fun addSource(model: Model, @ModelAttribute sourceFormData: F): Any {
        val config = mapConfig(sourceFormData)
        saveConfig(config)
        return getRedirectAfterSaveView(config)
    }

    @PostMapping("/update-source")
    fun updateSource(model: Model, @ModelAttribute sourceFormData: F): Any {
        val config = mapConfig(sourceFormData)
        updateConfig(config)
        return getRedirectAfterSaveView(config)
    }

    protected open fun getRedirectAfterSaveView(config:T): Any =
            ModelAndView("redirect:${BVWebPaths.SETTINGS}")

    private fun saveConfig(config:T) {
        sourcesConfigProvider.save(config)
    }

    private fun updateConfig(config:T) {
        sourcesConfigProvider.update(config)
    }

    protected fun  getConfig(sourceName: String): T? =
            sourcesConfigProvider.getConfigByName(sourceName, getConfigClass())

    protected abstract fun mapConfig(sourceFormData: F): T
    protected abstract fun mapForm(config: T): F
    protected abstract fun getConfigClass(): Class<T>
}