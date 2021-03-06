package org.birdview.web.secrets

import org.birdview.storage.BVAbstractSourceConfig
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.web.BVWebPaths
import org.slf4j.LoggerFactory
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

abstract class AbstractSourceWebController<T : BVAbstractSourceConfig, F> (
        protected val sourceSecretsStorage: BVSourceSecretsStorage
) {
    private val log = LoggerFactory.getLogger(AbstractSourceWebController::class.java)

    abstract class AbstractSourceFormData(
            val sourceName:String,
            val type:String,
            val user: String
    )

    @GetMapping("/edit-secret")
    fun editSourceView1(model: Model, @RequestParam("sourceName") sourceName: String): String {
        val config = getConfig(sourceName)!!
        model.addAttribute("source", mapForm(config))

        return "secrets/edit-secret"
    }

    @PostMapping("/add-secret")
    fun addSource(model: Model, @ModelAttribute sourceFormData: F): Any {
        val config = mapConfig(sourceFormData)
        saveConfig(config)
        return getRedirectAfterSaveView(config)
    }

    @PostMapping("/update-secret")
    fun updateSource(model: Model, @ModelAttribute sourceFormData: F): Any {
        val config = mapConfig(sourceFormData)
        updateConfig(config)
        return getRedirectAfterSaveView(config)
    }

    protected open fun getRedirectAfterSaveView(config:T): Any =
            ModelAndView("redirect:${BVWebPaths.ADMIN_ROOT}")

    private fun saveConfig(config:T) {
        sourceSecretsStorage.create(config)
    }

    private fun updateConfig(config:T) {
        sourceSecretsStorage.update(config)
    }

    private fun  getConfig(sourceName: String): T? =
            sourceSecretsStorage.getConfigByName(sourceName, getConfigClass())

    protected abstract fun mapConfig(sourceFormData: F): T
    protected abstract fun mapForm(config: T): F
    protected abstract fun getConfigClass(): Class<T>
}