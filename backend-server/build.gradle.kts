plugins {
    application
}

application {
    mainClass.set("com.uroria.backend.server.Uroria")
}

val log4jVersion: String by project.extra
val sentryVersion: String by project.extra
val mongoDBVersion: String by project.extra
val lettuceVersion: String by project.extra

dependencies {
    implementation(project(":backend-plugin-api"))
    implementation(project(":backend-api"))

    implementation("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-iostreams:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-jul:${log4jVersion}")

    implementation("io.sentry:sentry:${sentryVersion}")
    implementation("org.mongodb:mongodb-driver-sync:${mongoDBVersion}")
    implementation("io.lettuce:lettuce-core:${lettuceVersion}")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("org.hotswapagent:hotswap-agent-core:1.4.1")
}

tasks {
    shadowJar {
        archiveFileName.set("Backend.jar")
    }
}