plugins {
    alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:citadel-paper"))
    api(project(":libraries:name-api"))
    compileOnly(libs.worldedit)

    testImplementation(libs.paper.api)
    testImplementation(libs.bundles.junit)
}

tasks.test {
    useJUnitPlatform()
}
