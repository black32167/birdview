package org.birdview.web.admin

import org.birdview.web.BVWebPaths
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping(BVWebPaths.ADMIN)
class BVAdminController {
    @GetMapping
    fun adminPage() =
            ModelAndView("redirect:${BVWebPaths.SECRETS}")
}