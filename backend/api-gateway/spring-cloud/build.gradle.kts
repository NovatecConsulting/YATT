plugins { id("com.novatecgmbh.spring-boot") }

dependencies {
  implementation("io.opentracing.contrib:opentracing-spring-jaeger-cloud-starter")
  implementation("org.springframework.cloud:spring-cloud-starter-gateway")
}
