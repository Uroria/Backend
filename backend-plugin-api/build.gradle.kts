plugins {
    `maven-publish`
}

java {
    withJavadocJar()
}

tasks {
    jar {
        manifest {
            attributes["Automatic-Module-Name"] = "com.uroria.backend.api"
        }
    }
}

dependencies {
    compileOnly(project(":backend-common"))
}

