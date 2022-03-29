plugins {
  id("java-platform")
}

group = "com.novatecgmbh.platform"

// allow the definition of dependencies to other platforms like the Axon BOM
javaPlatform.allowDependencies()

dependencies {
  api(platform("org.springframework.boot:spring-boot-dependencies:2.7.0-M3"))
  api(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.2-SNAPSHOT"))
  api(platform("org.axonframework:axon-bom:4.5.11"))

  constraints {
    api("io.opentracing.contrib:opentracing-spring-jaeger-cloud-starter:3.3.1")
    api("io.opentracing.contrib:opentracing-spring-jaeger-starter:3.2.2")
    api("io.projectreactor:reactor-core:3.4.8")
    api("javax.persistence:javax.persistence-api:2.2")
  }
}
