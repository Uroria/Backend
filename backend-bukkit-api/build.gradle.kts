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

dependencies {
    implementation(project(":backend-api"))
}

