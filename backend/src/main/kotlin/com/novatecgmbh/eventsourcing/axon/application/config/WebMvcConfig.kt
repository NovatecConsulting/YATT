package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.security.UserPrincipalResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {

  override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
    resolvers.add(UserPrincipalResolver())
  }
}
