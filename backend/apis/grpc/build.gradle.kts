import com.google.protobuf.gradle.*

plugins {
  id("com.google.protobuf") version "0.8.18"
  id("com.novatecgmbh.commons-kotlin")
  id("io.spring.dependency-management")
  kotlin("plugin.spring")
}

group = "${group}.apis"

dependencies {
  implementation("com.novatecgmbh.eventsourcing.axon.apis:api-common")
  implementation("com.novatecgmbh.eventsourcing.axon.common:api")
  implementation("com.novatecgmbh.eventsourcing.axon.common:auditing")
  implementation("com.novatecgmbh.eventsourcing.axon.company:api")
  implementation("com.novatecgmbh.eventsourcing.axon.project:api")
  implementation("com.novatecgmbh.eventsourcing.axon.user:api")

  implementation(platform("io.grpc:grpc-bom:1.43.2"))
  implementation("io.grpc:grpc-netty-shaded")
  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")

  implementation("net.devh:grpc-spring-boot-starter:2.13.1.RELEASE")
  implementation("org.axonframework:axon-spring-boot-starter")
  implementation("org.axonframework.extensions.kotlin:axon-kotlin")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
}

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:3.19.2" }
  plugins { id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.43.2" } }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.plugins {
        // Apply the "grpc" plugin whose spec is defined above, without options.
        id("grpc")
      }
    }
  }
}
