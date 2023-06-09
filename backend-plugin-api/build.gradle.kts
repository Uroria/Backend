plugins {
    `maven-publish`
}

java {
    withJavadocJar()
}

tasks {
    jar {
        manifest {
            attributes["Automatic-Module-Name"] = "com.uroria.backend.pluginapi"
        }
    }
}

dependencies {
    api(project(":backend-common"))
}

