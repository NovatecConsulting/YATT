package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.security.CustomUserAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.userdetails.UserDetailsService

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class SecurityConfig(val userDetailsService: UserDetailsService) : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    http.cors()
        .and()
        .headers()
        .httpStrictTransportSecurity()
        .disable()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(STATELESS)
        .and()
        .csrf()
        .disable()
        .formLogin()
        .disable()
        .httpBasic()
        .disable()
        .exceptionHandling()
        .and()
        .authorizeRequests()
        .antMatchers("/graphql/schema/**", "/graphiql/**", "/wsgraphql/**")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .oauth2ResourceServer()
        .jwt()
        .jwtAuthenticationConverter(customUserAuthenticationConverter())
  }

  @Bean
  fun customUserAuthenticationConverter(): CustomUserAuthenticationConverter =
      CustomUserAuthenticationConverter(userDetailsService)
}
