rootProject.name = "backend"

val projects = listOf(
        "backend-plugin-api",
        "backend-api",
        "backend-common",
        "backend-server",
        "backend-velocity-api",
        "backend-bukkit-api"
)

include(projects)
projects.forEach { project -> findProject(":$project")?.name = project }

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}