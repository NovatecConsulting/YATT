// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal() // if pluginManagement.repositories looks like this, it can be omitted as this is the default
    }
    includeBuild("../build-logic")
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
includeBuild("../platforms")
includeBuild("../common")

// == Define the inner structure of this component ==
rootProject.name = "user"
include("api")
include("application")
include("command-query")
include("web")