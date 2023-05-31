plugins {
    `maven-publish`
}

java {
    withJavadocJar()
}

tasks {
    jar {
        manifest {
            attributes["Automatic-Module-Name"] = "com.uroria.backend.velocity"
        }
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

val velocityVersion: String by project.extra
val sentryVersion: String by project.extra

dependencies {
    api(project(":backend-api"))

    compileOnly("com.velocitypowered:velocity-api:${velocityVersion}")

    implementation("io.sentry:sentry:${sentryVersion}")
}

