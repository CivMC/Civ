plugins {
    alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":plugins:civmodcore-paper"))
    api(project(":libraries:name-api"))
    compileOnly(libs.worldedit)
}
