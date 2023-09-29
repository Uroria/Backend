java {
    withJavadocJar()
    withSourcesJar()
}

val baseVersion: String by project.extra
val problemoVersion: String by project.extra
dependencies {
    api("com.uroria:base:${baseVersion}")
    annotationProcessor("com.uroria:base:${baseVersion}")

    api("com.uroria:Problemo:${problemoVersion}")
}