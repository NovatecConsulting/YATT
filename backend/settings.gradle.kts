// This is an empty umbrella build including all the component builds.
// This build is not necessarily needed. The component builds work independently.
rootProject.name = "backend"

includeBuild("platforms")
includeBuild("build-logic")

includeBuild("data-import")

includeBuild("apis")
includeBuild("services")

includeBuild("clients")
