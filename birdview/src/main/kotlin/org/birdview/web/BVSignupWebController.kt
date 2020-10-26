package org.birdview.web

import org.birdview.security.PasswordUtils
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping(BVWebPaths.SIGNUP)
class BVSignupWebController (
        val userStorage: BVUserStorage
) {
    class SignupFormData(
            val user: String,
            val password: String,
            val email: String
    )

    @GetMapping
    fun showForm(): String {
        return "/signup"
    }

    @PostMapping
    fun signup(@ModelAttribute formData: SignupFormData): ModelAndView {
        userStorage.create(
                formData.user,
                BVUserSettings(
                        passwordHash = PasswordUtils.hash(formData.password),
                        email = formData.email))
        return ModelAndView("redirect:${BVWebPaths.LOGIN}")
    }
}
