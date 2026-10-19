plugins {
    alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    api(libs.configurate.yaml)
    api(libs.hikaricp)
    api(libs.mariadb.client)
    api(libs.rabbitmq.client)
    api(project(":libraries:name-api"))
    api(project(":libraries:namelayer-sync"))
}
