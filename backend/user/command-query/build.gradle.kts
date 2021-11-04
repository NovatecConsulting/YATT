plugins {
    id("com.novatecgmbh.command-query-module")
}

group = "${group}.user"

dependencies {
    api(project(":api"))

    api("com.novatecgmbh.eventsourcing.axon.common:command-query")
}