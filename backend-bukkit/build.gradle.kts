val sentryVersion: String by project.extra

dependencies {
    api(project(":backend-impl"))

    implementation("io.sentry:sentry:${sentryVersion}")
    compileOnly("org.purpurmc.purpur:purpur-api:1.20.1-R0.1-SNAPSHOT")
}

