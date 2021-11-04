plugins {
    id("com.novatecgmbh.web-module")
}

group = "${group}.user"

dependencies {
    api(project(":api"))

    implementation("com.novatecgmbh.eventsourcing.axon.common:web")
}