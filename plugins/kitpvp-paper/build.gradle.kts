plugins {
    alias(libs.plugins.paper.userdev)
    alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:finale-paper"))
    compileOnly(libs.luckperms.api)

    compileOnly(files("../../deployment/src/paper-plugins/BreweryX-3.6.3.jar"))
    compileOnly(libs.aswm.api)
    compileOnly(libs.placeholderapi)
}
