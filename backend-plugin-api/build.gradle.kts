plugins {
    `maven-publish`
}

java {
    withJavadocJar()
}

dependencies {
    api(project(":backend-common"))
}

