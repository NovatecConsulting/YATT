import com.google.protobuf.gradle.*

plugins {
  id("com.google.protobuf") version "0.8.18"
  id("com.novatecgmbh.commons-kotlin")
}

group = "${group}.apis"

dependencies {
  implementation(platform("io.grpc:grpc-bom:1.43.2"))
  implementation("io.grpc:grpc-netty-shaded")
  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
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
