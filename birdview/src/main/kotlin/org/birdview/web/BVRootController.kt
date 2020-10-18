package org.birdview.web

import org.birdview.security.UserContext
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class BVRootController {
    @GetMapping
    fun navigateHome() =
            if (UserContext.isAdmin()) {
                ModelAndView("redirect:${BVWebPaths.ADMIN_ROOT}")
            } else {
                ModelAndView("redirect:${BVWebPaths.EXPLORE}")
            }
}