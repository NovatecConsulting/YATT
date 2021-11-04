plugins {
    id("com.novatecgmbh.web-module")
}

group = "${group}.common"

dependencies {
    api(project(":api"))

    api("com.novatecgmbh.eventsourcing.axon.user:api")
}

