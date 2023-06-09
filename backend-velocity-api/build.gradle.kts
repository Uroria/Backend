plugins {
    `maven-publish`
}

java {
    withJavadocJar()
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

