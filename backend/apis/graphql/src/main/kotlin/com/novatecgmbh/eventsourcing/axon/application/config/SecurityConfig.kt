package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.security.CustomUserAuthenticationConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class SecurityConfig(val userDetailsService: UserDetailsService) : WebSecurityConfigurerAdapter() {

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private val issuer: String? = null

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
            .antMatchers("/graphql/schema/**", "/graphiql/**")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .decoder(jwtDecoder())
            .jwtAuthenticationConverter(customUserAuthenticationConverter())
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder: NimbusJwtDecoder = JwtDecoders.fromIssuerLocation(issuer) as NimbusJwtDecoder
        jwtDecoder.setJwtValidator(DelegatingOAuth2TokenValidator(JwtTimestampValidator()))
        return jwtDecoder
    }

    @Bean
    fun customUserAuthenticationConverter(): CustomUserAuthenticationConverter =
        CustomUserAuthenticationConverter(userDetailsService)
}
