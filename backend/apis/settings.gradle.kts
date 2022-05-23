pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
  }
  includeBuild("../build-logic")

  val springBootVersion: String by settings
  val kotlinVersion: String by settings
  val springDependencyManagementPluginVersion: String by settings
  plugins {
    id("io.spring.dependency-management") version springDependencyManagementPluginVersion
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
includeBuild("../services")

rootProject.name = "apis"
include("common")
include("graphql")
include("grpc")
include("grpc-lib")
include("rest")
include("rsocket")
