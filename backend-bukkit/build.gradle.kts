dependencies {
    api(project(":backend-impl"))
    annotationProcessor(project(":backend-impl"))

    compileOnly("org.purpurmc.purpur:purpur-api:1.20.1-R0.1-SNAPSHOT")
}