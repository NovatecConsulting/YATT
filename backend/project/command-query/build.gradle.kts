plugins {
    id("com.novatecgmbh.command-query-module")
}

group = "${group}.project"

dependencies {
    api(project(":api"))

    api("com.novatecgmbh.eventsourcing.axon.common:auditing")
    api("com.novatecgmbh.eventsourcing.axon.common:command-query")

    testImplementation("com.novatecgmbh.eventsourcing.axon.common:auditing")
}