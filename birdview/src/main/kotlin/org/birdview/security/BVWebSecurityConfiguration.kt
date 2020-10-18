package org.birdview.security

import org.birdview.web.BVWebPaths
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import javax.inject.Inject

@Configuration
@EnableWebSecurity
open class BVWebSecurityConfiguration : WebSecurityConfigurerAdapter() {
    @Inject
    private lateinit var userDetailsService: UserDetailsService

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
//                .csrf().ignoringAntMatchers("/logout")
//                .and()
                .authorizeRequests()
                    .antMatchers( "/**/*.js", "/**/*.css").permitAll()
                    .mvcMatchers(BVWebPaths.SIGNUP).permitAll()
                    .antMatchers("/**").authenticated()
                    .antMatchers( "${BVWebPaths.ADMIN_ROOT}/**").hasRole(Roles.ADMIN)
                    .antMatchers("${BVWebPaths.USER_ROOT}/**").hasRole(Roles.USER)

                .and()
                .formLogin().loginPage("/login").permitAll()//loginProcessingUrl("/login").
                .and()
                .logout().permitAll()
    }

    override fun userDetailsService(): UserDetailsService {
        return userDetailsService
    }
}