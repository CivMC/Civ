import org.gradle.api.tasks.Copy

plugins {
    alias(libs.plugins.shadow)
}

version = "1.0"

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    implementation(libs.configurate.yaml)
    api(libs.rabbitmq.client)
    compileOnly(libs.luckperms.api)
}
