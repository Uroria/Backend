import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
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

subprojects {
    apply<JavaLibraryPlugin>()
    apply<ShadowPlugin>()
    apply<MavenPublishPlugin>()

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.purpurmc.org/snapshots")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/maven-snapshots/")
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

    tasks {
        test {
            useJUnitPlatform()
            reports {
                junitXml.required.set(true)
            }
        }

        shadowJar {
            transform(Log4j2PluginsCacheFileTransformer::class.java)
        }

        jar {
            manifest {
                attributes["Multi-Release"] = "true"
            }
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
