package org.birdview.web.secrets

import org.birdview.source.BVTaskSource
import org.birdview.source.SourceType
import org.birdview.storage.BVSourceSecretsStorage
import org.birdview.web.BVTemplatePaths
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
class BVSourceSecretListWebController(
        private val sourceSecretsStorage: BVSourceSecretsStorage,
        private val sources: List<BVTaskSource>
) {
    class SourceSettingView(
            val name: String,
            val type: SourceType)


    @GetMapping("add-secret")
    fun addSource(model: Model): String {
        model.addAttribute("sourceTypes", sources.map { it.getType().name.toLowerCase() })

        return BVTemplatePaths.ADD_SECRET
    }

    @GetMapping("/delete")
    fun deleteSource(model: Model, @RequestParam("sourceName") sourceName: String): ModelAndView {
        sourceSecretsStorage.delete(sourceName)

        return ModelAndView("redirect:${BVWebPaths.ADMIN_ROOT}")
    }
}