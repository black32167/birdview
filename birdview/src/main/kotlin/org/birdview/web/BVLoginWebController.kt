package org.birdview.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class BVLoginWebController {
    @GetMapping(BVWebPaths.LOGIN)
    fun showForm(model: Model): String {
        return "/login"
    }
}