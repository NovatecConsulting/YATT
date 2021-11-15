plugins {
  id("com.novatecgmbh.commons-kotlin")
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  kotlin("plugin.spring")
}

dependencies {
  implementation("io.opentracing.contrib:opentracing-spring-jaeger-cloud-starter")
  implementation("org.springframework.cloud:spring-cloud-starter-gateway")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
