plugins { `kotlin-dsl` }

dependencies {
  implementation(platform("com.novatecgmbh.platform:plugins-platform"))

  implementation(project(":spring-boot"))
}
