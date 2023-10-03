val rabbitMqVersion: String by project.extra

dependencies {
    api(project(":backend-api"))
    annotationProcessor(project(":backend-api"))

    api("com.rabbitmq:amqp-client:${rabbitMqVersion}")
}

