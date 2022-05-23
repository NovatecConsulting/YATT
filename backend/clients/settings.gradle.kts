pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
  }
  includeBuild("../build-logic")

  val springBootVersion: String by settings
  val kotlinVersion: String by settings
  plugins {
    id("org.springframework.boot") version springBootVersion
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven ("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
  }
}

includeBuild("../platforms")

rootProject.name = "clients"
include("grpc")
include("rsocket")
