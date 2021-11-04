plugins {
    id("java-platform")
}

group = "com.novatecgmbh.platform"

// allow the definition of dependencies to other platforms like the JUnit 5 BOM
javaPlatform.allowDependencies()

dependencies {
    api(platform("org.junit:junit-bom:5.8.1"))

    constraints {
	    api("com.tngtech.archunit:archunit-junit5:0.21.0")
	    api("org.apache.commons:commons-lang3:3.11")
	    api("io.mockk:mockk:1.12.0")
    }
}
