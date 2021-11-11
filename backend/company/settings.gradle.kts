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
includeBuild("../project")
includeBuild("../user")

// == Define the inner structure of this component ==
rootProject.name = "company"
include("api")
include("application")
include("application-without-web")
include("command-query")
include("web")

