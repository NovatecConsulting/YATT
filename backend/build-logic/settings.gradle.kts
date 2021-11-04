dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
includeBuild("../platforms")

rootProject.name = "build-logic"
include("commons-java")
include("commons-kotlin")

include("axon-spring-boot-application")

include("api-module")
include("command-query-module")
include("web-module")
