plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform("com.novatecgmbh.platform:plugins-platform"))

    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin")
}