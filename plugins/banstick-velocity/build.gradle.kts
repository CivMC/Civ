plugins {
    alias(libs.plugins.shadow)
}

group = "xyz.huskydog"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    implementation(libs.ipaddress)
    implementation(libs.jsoup)
}
