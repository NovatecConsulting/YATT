plugins {
    id("com.novatecgmbh.command-query-module")
}

group = "${group}.company"

dependencies {
    api(project(":api"))

    api("com.novatecgmbh.eventsourcing.axon.common:command-query")
}