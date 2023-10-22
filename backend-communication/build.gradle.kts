val rabbitMqVersion: String by project.extra
val areVersion: String by project.extra

dependencies {
    api(project(":backend-api"))
    annotationProcessor(project(":backend-api"))

    api("com.rabbitmq:amqp-client:${rabbitMqVersion}")

    api("com.uroria.are:are-library:${areVersion}")
}