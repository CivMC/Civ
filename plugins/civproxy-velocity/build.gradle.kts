plugins {
    alias(libs.plugins.shadow)
}
version = "1.0.0"

dependencies {
    compileOnly(libs.velocity.api)
    compileOnly(libs.ajQueue.api)
    api(libs.hikaricp)
    api(libs.configurate.yaml)
    compileOnly(libs.luckperms.api)
    annotationProcessor(libs.velocity.api)
    api(project(":libraries:name-api"))
    compileOnly(project(":plugins:zorweth-velocity"))
}
