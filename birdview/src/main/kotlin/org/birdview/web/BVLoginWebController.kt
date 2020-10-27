package org.birdview.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class BVLoginWebController {
    @GetMapping(BVWebPaths.LOGIN)
    fun showForm(@RequestParam("error") error: String?, model: Model): String {
        if (error != null) {
            model.addAttribute("errorMessage", "Username or password not valid")
        }
        return "/login"
    }
}