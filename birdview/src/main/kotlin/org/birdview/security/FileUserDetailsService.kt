package org.birdview.security

import org.birdview.storage.BVUserStorage
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import javax.inject.Named

@Named
class FileUserDetailsService (
        val userStorage: BVUserStorage,
        @Value("\${birdview.admin.password}") val adninPassword: String
): UserDetailsService {
    companion object {
        const val ADMIN = "bv-admin"
    }
    @ExperimentalStdlibApi
    override fun loadUserByUsername(username: String): UserDetails {
        if (ADMIN == username) {
            return User.withUsername(ADMIN)
                    .password("{SHA-256}${PasswordUtils.hash(adninPassword)}")
                    .roles(Roles.ADMIN)
                    .build()
        }
        val userSettings = userStorage.getUserSettings(username)
        return User.withUsername(username)
                .password("{SHA-256}${userSettings.passwordHash}")
                .roles(Roles.USER)
                .build()
    }
}