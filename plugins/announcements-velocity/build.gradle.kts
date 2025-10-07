plugins {
    alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    api(libs.cron.utils)
    api(libs.configurate.yaml)
}
