rootProject.name = "backend"

val projects = listOf(
        "backend-impl",
        "backend-api",
        "backend-server",
        "backend-velocity",
        "backend-bukkit"
)

include(projects)
projects.forEach { project -> findProject(":$project")?.name = project }

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}