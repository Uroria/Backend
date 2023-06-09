import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

allprojects {
    group = "com.uroria"
    version = "0.0.1"
}

val junitVersion: String by project.extra
val simplixStorageVersion: String by project.extra
val slf4jVersion: String by project.extra
val adventureVersion: String by project.extra
val pulsarVersion: String by project.extra

subprojects {
    apply<JavaLibraryPlugin>()
    apply<ShadowPlugin>()

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(16))
        }
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven {
            url = uri("https://gitlab.zyonicsoftware.com/api/v4/groups/622/-/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                if (System.getenv("CI") == "true") {
                    name = "Private-Token"
                    value = System.getenv("U_PIPELINE_DEFAULT_TOKEN")
                } else {
                    name = "Private-Token"
                    value = project.properties["uroriaGitlabToken"].toString()
                }
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")

        api("org.slf4j:slf4j-api:$slf4jVersion")

        api("com.github.simplix-softworks:simplixstorage:${simplixStorageVersion}")

        implementation("net.kyori:adventure-api:${adventureVersion}")
        implementation("net.kyori:adventure-text-serializer-gson:${adventureVersion}")
        implementation("net.kyori:adventure-text-minimessage:${adventureVersion}")
        implementation("net.kyori:adventure-text-serializer-plain:${adventureVersion}")
        implementation("net.kyori:adventure-text-serializer-legacy:${adventureVersion}")

        implementation("org.apache.pulsar:pulsar-client:${pulsarVersion}")
    }

    tasks {
        test {
            useJUnitPlatform()
            reports {
                junitXml.required.set(true)
            }
        }

        shadowJar {
            transform(Log4j2PluginsCacheFileTransformer::class.java)
            relocate("de.leonhard.storage", "com.uroria.storage")
        }
    }
}
