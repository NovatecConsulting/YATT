plugins {
  id("com.google.protobuf") version "0.8.18"
  id("com.novatecgmbh.commons-kotlin")
  id("io.spring.dependency-management")
  kotlin("plugin.spring")
}

group = "${group}.apis"

dependencies {
  implementation("com.novatecgmbh.eventsourcing.axon.apis:api-common")
  implementation("com.novatecgmbh.eventsourcing.axon.apis:grpc-lib")

  implementation("io.projectreactor:reactor-core")
  implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")

  implementation("net.devh:grpc-spring-boot-starter:2.13.1.RELEASE")
  implementation("org.axonframework:axon-spring-boot-starter")
  implementation("org.axonframework.extensions.kotlin:axon-kotlin")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
}
