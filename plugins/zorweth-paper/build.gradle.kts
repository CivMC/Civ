plugins {
    alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:citadel-paper"))
    compileOnly(project(":plugins:bastion-paper"))
    compileOnly(project(":plugins:combattagplus-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:exilepearl-paper"))
    api(project(":libraries:name-api"))
    compileOnly(libs.worldedit)
    compileOnly(files("../../ansible/src/paper-plugins/BreweryX-3.6.3.jar"))

    testImplementation(libs.paper.api)
    testImplementation(libs.bundles.junit)
}

tasks.test {
    useJUnitPlatform()
}
