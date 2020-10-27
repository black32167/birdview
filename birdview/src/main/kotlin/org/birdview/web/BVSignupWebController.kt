package org.birdview.web

import org.birdview.security.PasswordUtils
import org.birdview.storage.BVUserStorage
import org.birdview.storage.model.BVUserSettings
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
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
    companion object {
        private const val SIGNUP = "/signup"
    }
    class SignupFormData(
            val user: String,
            val password: String,
            val email: String
    )

    @GetMapping
    fun showForm(): String {
        return SIGNUP
    }

    @PostMapping
    fun signup(@ModelAttribute formData: SignupFormData, model:Model): String {
        try {
            userStorage.create(
                    formData.user,
                    BVUserSettings(
                            passwordHash = PasswordUtils.hash(formData.password),
                            email = formData.email))
        } catch (e: BVUserStorage.UserStorageException) {
            model.addAttribute("errorMessage", "User already exists")
            return SIGNUP
        }
        return "redirect:${BVWebPaths.LOGIN}"
    }
}
