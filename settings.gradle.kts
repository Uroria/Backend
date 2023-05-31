rootProject.name = "Backend"

include(
        "backend-plugin-api",
        "backend-api",
        "backend-common",
        "backend-server",
        "backend-velocity-api",
        "backend-bukkit-api"
)

findProject(":backend-plugin-api")?.name = "backend-plugin-api"
findProject(":backend-common")?.name = "backend-common"
findProject(":backend-server")?.name = "backend-server"
findProject(":backend-api")?.name = "backend-api"
findProject(":backend-velocity-api")?.name = "backend-velocity-api"
findProject(":backend-bukkit-api")?.name = "backend-bukkit-api"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}