plugins {
    alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    implementation(libs.cron.utils)
    implementation(libs.configurate.yaml)
}
