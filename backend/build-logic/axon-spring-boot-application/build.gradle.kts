plugins { `kotlin-dsl` }

dependencies {
  implementation(platform("com.novatecgmbh.platform:plugins-platform"))

  implementation(project(":commons-kotlin"))

  implementation("io.spring.gradle:dependency-management-plugin")
  implementation("org.springframework.boot:org.springframework.boot.gradle.plugin")
  implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin")
  implementation(
      "org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin")
}
