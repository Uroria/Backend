plugins {
    application
    id("org.graalvm.buildtools.native") version "0.9.27"
}

application {
    mainClass.set("com.uroria.backend.service.Bootstrap")
}

val mongoDBVersion: String by project.extra
val lettuceVersion: String by project.extra
val jlineVersion: String by project.extra
val terminalConsoleAppenderVersion: String by project.extra
val log4jVersion: String by project.extra

dependencies {
    implementation(project(":backend-impl"))
    annotationProcessor(project(":backend-impl"))

    implementation("org.mongodb:mongodb-driver-sync:${mongoDBVersion}")
    implementation("io.lettuce:lettuce-core:${lettuceVersion}")

    implementation("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-iostreams:${log4jVersion}")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    runtimeOnly("org.jline:jline-terminal-jansi:${jlineVersion}")
    implementation("net.minecrell:terminalconsoleappender:${terminalConsoleAppenderVersion}")
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(20))
            })
        }
    }
}

tasks {
    shadowJar {
        archiveFileName.set("Backend.jar")
    }
}