plugins {
    id("com.github.johnrengelman.shadow")
}

version = "1.0.0"

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    implementation(libs.cron.utils)
    implementation(libs.snakeyaml)
}
