// This is an empty umbrella build including all the component builds.
// This build is not necessarily needed. The component builds work independently.
rootProject.name = "my-composite"

includeBuild("platforms")
includeBuild("build-logic")

includeBuild("common")
includeBuild("company")
includeBuild("project")
includeBuild("user")
