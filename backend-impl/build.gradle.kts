val pulsarVersion: String by project.extra

dependencies {
    api(project(":backend-api"))
    annotationProcessor(project(":backend-api"))

    api("org.apache.pulsar:pulsar-client:${pulsarVersion}")
}

