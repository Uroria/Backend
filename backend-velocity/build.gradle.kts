repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

val velocityVersion: String by project.extra

dependencies {
    api(project(":backend-impl"))
    annotationProcessor(project(":backend-impl"))

    compileOnly("com.velocitypowered:velocity-api:${velocityVersion}")
    annotationProcessor("com.velocitypowered:velocity-api:${velocityVersion}")
}