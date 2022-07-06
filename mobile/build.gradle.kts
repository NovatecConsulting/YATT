buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        val kotlinVersion = "1.7.0"
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
        classpath(kotlin("serialization", version = kotlinVersion))
        classpath("com.apollographql.apollo3:apollo-gradle-plugin:3.3.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}