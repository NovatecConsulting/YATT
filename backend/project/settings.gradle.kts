// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal() // if pluginManagement.repositories looks like this, it can be omitted as this is the default
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

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
includeBuild("../platforms")
includeBuild("../user")
includeBuild("../company")

// == Define the inner structure of this component ==
rootProject.name = "project"
include("api")
include("application")
include("command-query")
include("rsocket")
include("web")
