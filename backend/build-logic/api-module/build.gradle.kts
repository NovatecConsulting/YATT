plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform("com.novatecgmbh.platform:plugins-platform"))
    implementation(project(":commons-kotlin"))
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin")
}
