plugins {
    id("com.novatecgmbh.command-query-module")
}

group = "${group}.common"

dependencies {
    api(project(":api"))
}