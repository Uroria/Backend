plugins {
    application
}

application {
    mainClass.set("")
}

val log4jVersion: String by project.extra
val sentryVersion: String by project.extra
val mongoDBVersion: String by project.extra
val lettuceVersion: String by project.extra

dependencies {
    implementation(project(":backend-plugin-api"))
    implementation(project(":backend-common"))

    implementation("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-iostreams:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-jul:${log4jVersion}")

    implementation("io.sentry:sentry:${sentryVersion}")
    implementation("org.mongodb:mongodb-driver-sync:${mongoDBVersion}")
    implementation("io.lettuce:lettuce-core:${lettuceVersion}")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")
}