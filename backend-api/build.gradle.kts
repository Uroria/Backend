java {
    withJavadocJar()
    withSourcesJar()
}

val fastUtilVersion: String by project.extra
dependencies {
    api("it.unimi.dsi:fastutil:${fastUtilVersion}")
}