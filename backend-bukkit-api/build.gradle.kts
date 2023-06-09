plugins {
    `maven-publish`
}

java {
    withJavadocJar()
}

tasks {
    jar {
        manifest {
            attributes["Automatic-Module-Name"] = "com.uroria.backend.bukkit"
        }
    }
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}


val sentryVersion: String by project.extra

dependencies {
    api(project(":backend-api"))

    implementation("io.sentry:sentry:${sentryVersion}")

    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
}

