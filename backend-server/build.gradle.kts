plugins {
    application
}

application {
    mainClass.set("com.uroria.backend.service.Bootstrap")
}

val log4jVersion: String by project.extra
val sentryVersion: String by project.extra
val mongoDBVersion: String by project.extra
val lettuceVersion: String by project.extra
val jlineVersion: String by project.extra
val terminalConsoleAppenderVersion: String by project.extra

dependencies {
    implementation(project(":backend-impl"))

    implementation("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-iostreams:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-jul:${log4jVersion}")

    implementation("io.sentry:sentry:${sentryVersion}")
    implementation("org.mongodb:mongodb-driver-sync:${mongoDBVersion}")
    implementation("io.lettuce:lettuce-core:${lettuceVersion}")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    runtimeOnly("org.jline:jline-terminal-jansi:${jlineVersion}")
    implementation("net.minecrell:terminalconsoleappender:${terminalConsoleAppenderVersion}")
}

tasks {
    shadowJar {
        archiveFileName.set("Backend.jar")
    }
}