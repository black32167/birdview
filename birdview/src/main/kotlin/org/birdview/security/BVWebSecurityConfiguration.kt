package org.birdview.security

import org.birdview.web.BVWebPaths
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import javax.inject.Inject

@Configuration
@EnableWebSecurity
open class BVWebSecurityConfiguration: WebSecurityConfigurerAdapter() {
    private val log = LoggerFactory.getLogger(BVWebSecurityConfiguration::class.java)

    @Inject
    private lateinit var userDetailsService: UserDetailsService

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
                .antMatchers("/**/*.js", "/**/*.css").permitAll()
                .mvcMatchers(BVWebPaths.SIGNUP).permitAll()
                .mvcMatchers(BVWebPaths.ADMIN_ROOT).hasRole(Roles.ADMIN)
                .mvcMatchers(BVWebPaths.USER_ROOT).hasRole(Roles.USER)
                .antMatchers("/**").authenticated()
            .and()
                .formLogin().loginPage("/login").permitAll()//loginProcessingUrl("/login").
            .and()
                .logout().permitAll()
    }

    override fun userDetailsService(): UserDetailsService {
        return userDetailsService
    }

    @EventListener
    fun onUserSuccessAuthenticated(event: AuthenticationSuccessEvent) {
        log.info("User successfully logged in:${event.authentication.name}")
    }

    @EventListener
    fun onUserFailedAuthenticated(event: AbstractAuthenticationFailureEvent) {
        log.error("User failed to login:${event.authentication.name}")
    }
}