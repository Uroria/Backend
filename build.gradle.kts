import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "com.uroria.backend"
    version = project.properties["projectVersion"].toString()
}

val jetbrainsAnnotationsVersion: String by project.extra
val lombokVersion: String by project.extra
val junitVersion: String by project.extra
val simplixStorageVersion: String by project.extra
val slf4jVersion: String by project.extra
val adventureVersion: String by project.extra
val pulsarVersion: String by project.extra

subprojects {
    apply<JavaLibraryPlugin>()
    apply<ShadowPlugin>()
    apply<MavenPublishPlugin>()

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
        api("org.jetbrains:annotations:${jetbrainsAnnotationsVersion}")
        api("org.projectlombok:lombok:${lombokVersion}")
        annotationProcessor("org.jetbrains:annotations:${jetbrainsAnnotationsVersion}")
        annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

        api("org.slf4j:slf4j-api:$slf4jVersion")

        implementation("com.uroria.nutshell:nutshell-plugin:0.0.7-alpha")

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

        jar {
            manifest {
                attributes["Multi-Release"] = "true"
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }

        repositories {
            maven {
                url = uri("${System.getenv("CI_API_V4_URL")}/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
                name = "GitLab"
                if (System.getenv("CI") == "true") {
                    credentials(HttpHeaderCredentials::class) {
                        name = "Job-Token"
                        value = System.getenv("CI_JOB_TOKEN")
                    }
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }
}
