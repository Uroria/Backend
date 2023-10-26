dependencies {
    api(project(":backend-cache"))
    annotationProcessor(project(":backend-cache"))
}

tasks.shadowJar {
    minimize()
    exclude("Log4j-*", "*.md")
}