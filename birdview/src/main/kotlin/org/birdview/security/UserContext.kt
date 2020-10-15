package org.birdview.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

object UserContext {
    fun getUserName(): String = getUser()?.username!!

    fun isAdmin(): Boolean = getUser()?.authorities
            ?.any { it.authority == "ROLE_${Roles.ADMIN}" }
            ?: false

    fun isSet() = getUser()?.username != null

    private fun getUser(): User? = (SecurityContextHolder.getContext()
            .authentication.principal as? User)
}